package ir.msob.manak.memory.repositorymemory;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.domain.model.memory.repositorymemory.RepositoryMemory;
import ir.msob.manak.domain.service.client.RmsClient;
import ir.msob.manak.domain.service.properties.ManakProperties;
import ir.msob.manak.memory.common.ChunkService;
import ir.msob.manak.memory.common.OverviewGenerator;
import ir.msob.manak.memory.common.VectorDocumentFactory;
import ir.msob.manak.memory.util.ZipExtractor;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RepositoryMemoryService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryMemoryService.class);

    private final RmsClient rmsClient;
    private final RepositoryOverviewMemoryVectorRepository overviewRepository;
    private final RepositoryChunkMemoryVectorRepository chunkRepository;
    private final ChunkService chunkService;
    private final VectorDocumentFactory vectorFactory;
    private final OverviewGenerator overviewGenerator;
    private final ManakProperties manakProperties;

    public Mono<RepositoryMemory> save(RepositoryMemory dto, User user) {
        log.info("Start indexing repoId={} branch={}", dto.getRepositoryId(), dto.getBranch());

        return downloadZipBytes(dto, user)
                .map(this::extractFiles)
                .map(files -> enrichWithChunks(dto, files))
                .map(files -> enrichWithOverview(dto, files))
                .thenReturn(dto)
                .doOnSuccess(r -> log.info("Indexed repository {} successfully", dto.getRepositoryId()))
                .doOnError(e -> log.error("Failed to index repository {}: {}", dto.getRepositoryId(), e.getMessage(), e));
    }


    public List<VectorDocument> overviewQuery(QueryRequest request, User user) {
        log.debug("Running overviewQuery: {}", request);
        return overviewRepository.query(request);
    }

    public List<VectorDocument> chunkQuery(QueryRequest request, User user) {
        log.debug("Running chunkQuery: {}", request);
        return chunkRepository.query(request);
    }

    /**
     * Download ZIP bytes from RMS client.
     */
    private Mono<byte[]> downloadZipBytes(RepositoryMemory dto, User user) {
        return DataBufferUtils.join(
                        rmsClient.downloadBranch(dto.getRepositoryId(), dto.getBranch(), user)
                                .subscribeOn(Schedulers.boundedElastic())
                )
                .map(db -> {
                    byte[] bytes = new byte[db.readableByteCount()];
                    db.read(bytes);
                    DataBufferUtils.release(db);
                    return bytes;
                });
    }

    /**
     * Extract indexable files from ZIP.
     */
    private Map<String, byte[]> extractFiles(byte[] zipBytes) {
        return ZipExtractor.extractIndexableFiles(zipBytes, manakProperties.getMemory().getRepository().getFileExtensions());
    }

    /**
     * Chunk all files and store them in vector DB.
     */
    private Map<String, byte[]> enrichWithChunks(RepositoryMemory dto, Map<String, byte[]> files) {

        List<VectorDocument> chunks = files.entrySet().stream()
                .flatMap(entry -> vectorFactory.fromChunks(
                        "repositoryId",
                        dto.getRepositoryId(),
                        entry.getKey(),
                        chunkService.chunkBytes(entry.getValue(), entry.getKey())
                ).stream())
                .toList();

        log.debug("Saving {} chunk documents for repoId={}", chunks.size(), dto.getRepositoryId());
        chunkRepository.save(chunks);

        return files;
    }

    /**
     * Look for README and create overview chunk.
     */
    private Map<String, byte[]> enrichWithOverview(RepositoryMemory dto, Map<String, byte[]> files) {

        files.entrySet().stream()
                .filter(entry -> isReadme(entry.getKey()))
                .findFirst()
                .ifPresent(readme -> {
                    List<VectorDocument> readmeChunks = vectorFactory.fromChunks(
                            "repositoryId",
                            dto.getRepositoryId(),
                            readme.getKey(),
                            chunkService.chunkBytes(readme.getValue(), readme.getKey())
                    );

                    VectorDocument overview = overviewGenerator.generate(
                            "repositoryId",
                            dto.getRepositoryId(),
                            readme.getKey(),
                            readmeChunks
                    );

                    overviewRepository.save(overview);

                    log.debug("Saved overview for repoId={} source={}", dto.getRepositoryId(), readme.getKey());
                });

        return files;
    }

    private boolean isReadme(String path) {
        return manakProperties.getMemory().getRepository().getReadMeFileName().stream().anyMatch(path::endsWith);
    }

}
