package ir.msob.manak.memory.repositorymemory;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;
import ir.msob.jima.crud.api.restful.client.domain.DomainCrudWebClient;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.service.jima.service.IdService;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.domain.model.memory.repositorymemory.RepositoryMemory;
import ir.msob.manak.domain.model.util.chunk.ChunkFile;
import ir.msob.manak.domain.model.util.chunk.Chunker;
import ir.msob.manak.domain.service.client.RmsClient;
import ir.msob.manak.domain.service.properties.ManakProperties;
import ir.msob.manak.memory.common.SummarizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
@RequiredArgsConstructor
public class RepositoryMemoryService {

    private static final Logger log = LoggerFactory.getLogger(RepositoryMemoryService.class);
    // allowed extensions set (mirror of your REPO_INDEX_EXTS)
    private static final Set<String> REPO_INDEX_EXTS = Set.of(
            ".java", ".kt", ".xml", ".yml", ".yaml", ".properties", ".md", ".txt",
            ".py", ".js", ".ts", ".json", ".html", ".css", ".gradle", ".groovy",
            ".pom", ".sql", ".sh", ".bash", "dockerfile"
    );
    private static final Set<String> READ_ME_FILES = Set.of(
            "README.md", "README.MD", "README", "readme.md", "readme"
    );
    private final RmsClient rmsClient;
    private final DomainCrudWebClient domainCrudWebClient;
    private final RepositoryOverviewMemoryVectorRepository overviewRepository;
    private final RepositoryChunkMemoryVectorRepository chunkRepository;
    private final ManakProperties manakProperties;
    private final IdService idService;
    private final SummarizerService summarizer;

    /**
     * Main entrypoint: loads document info, downloads file, chunks & indexes.
     */
    public Mono<RepositoryMemory> save(RepositoryMemory dto, User user) {
        log.info("Start indexing repoId={} branch={}", dto.getRepositoryId(), dto.getBranch());
        // Download zip as byte[] (collect DataBuffer)
        return DataBufferUtils.join(rmsClient.downloadBranch(dto.getRepositoryId(), dto.getBranch(), user)
                        .subscribeOn(Schedulers.boundedElastic()))
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                })
                .flatMap(bytes -> {
                    Map<String, byte[]> map = extract(bytes);
                    List<VectorDocument> chunkVectorDocuments = chunk(dto, map);
                    chunkRepository.save(chunkVectorDocuments);


                    Optional<Map.Entry<String, byte[]>> overview = map.entrySet().stream()
                            .filter(entry -> READ_ME_FILES.stream().anyMatch(s -> entry.getKey().endsWith(s)))
                            .findFirst();
                    overview.ifPresent(entry -> {
                        List<VectorDocument> overviewVectorDocuments = chunk(dto, entry.getKey(), entry.getValue());
                        List<String> strings = overviewVectorDocuments.stream()
                                .map(VectorDocument::getText)
                                .toList();
                        String sumery = summarizer.summarize(strings);
                        VectorDocument vectorDocument = VectorDocument.builder()
                                .id(idService.newId())
                                .text(sumery)
                                .metadata(Map.of(
                                        "repositoryId", dto.getRepositoryId(),
                                        "source", entry.getKey()
                                ))
                                .build();
                        overviewRepository.save(vectorDocument);

                    });


                    return Mono.just(dto);


                })
                .doOnSuccess(r -> log.info("Indexed repository {} successfully", dto.getRepositoryId()))
                .doOnError(e -> log.error("Failed to index repository {}: {}", dto.getRepositoryId(), e.getMessage(), e));
    }

    public Map<String, byte[]> extract(byte[] zipBytes) {
        Map<String, byte[]> fileMap = new LinkedHashMap<>();
        try (ZipInputStream zis = new ZipInputStream(new ByteArrayInputStream(zipBytes))) {
            ZipEntry ze;
            while ((ze = zis.getNextEntry()) != null) {
                if (ze.isDirectory()) continue;
                String name = ze.getName();
                String base = name.substring(name.lastIndexOf('/') + 1);
                if (base.startsWith(".")) continue;
                String ext = name.contains(".") ? name.substring(name.lastIndexOf('.')) : "";
                if (!REPO_INDEX_EXTS.contains(ext.toLowerCase())) {
                    log.debug("Skipping non-indexable file: {}", name);
                    continue;
                }
                // read entry
                byte[] buffer = zis.readAllBytes();
                fileMap.put(name, buffer);
            }
        } catch (IOException e) {
            log.error("Failed to unpack zip for : {}", e.getMessage(), e);
            throw new RuntimeException("Failed to unpack repository zip", e);
        }
        return fileMap;
    }

    /**
     * Chunk file content using Chunker
     */
    public List<VectorDocument> chunk(RepositoryMemory repositoryMemory, Map<String, byte[]> contentBytes) {
        log.debug("Chunking repositoryId={} with chunkSize={} and overlap={}",
                repositoryMemory.getRepositoryId(),
                manakProperties.getMemory().getChunk().getChunkSize(),
                manakProperties.getMemory().getChunk().getOverlap());

        return contentBytes.entrySet()
                .stream()
                .flatMap(entry -> chunk(repositoryMemory, entry.getKey(), entry.getValue()).stream())
                .toList();
    }

    private List<VectorDocument> chunk(RepositoryMemory repositoryMemory, String filePath, byte[] bytes) {
        List<ChunkFile> chunkFiles = Chunker.chunk(
                bytes,
                prepareFileType(filePath),
                manakProperties.getMemory().getChunk().getChunkSize(),
                manakProperties.getMemory().getChunk().getOverlap()
        );

        return chunkFiles
                .stream()
                .map(chunk -> prepareVectorDocument(repositoryMemory, chunk, filePath, chunkFiles.size()))
                .toList();
    }

    private Chunker.FileType prepareFileType(String key) {
        if (key == null || key.isBlank()) {
            return Chunker.FileType.GENERIC;
        }

        String fileName = key.substring(key.lastIndexOf('/') + 1);

        String ext = "";
        int dotIndex = fileName.lastIndexOf('.');
        if (dotIndex != -1 && dotIndex < fileName.length() - 1) {
            ext = fileName.substring(dotIndex + 1).toLowerCase();
        }

        return switch (ext) {
            case "md" -> Chunker.FileType.MARKDOWN;
            case "xml" -> Chunker.FileType.XML;
            case "pom" -> Chunker.FileType.POM;
            case "java" -> Chunker.FileType.JAVA;
            case "yml", "yaml" -> Chunker.FileType.YAML;
            case "properties" -> Chunker.FileType.PROPERTIES;
            case "ts", "tsx" -> Chunker.FileType.TYPESCRIPT;
            case "json" -> Chunker.FileType.JSON;
            default -> Chunker.FileType.GENERIC;
        };
    }


    /**
     * Convert ChunkFile → VectorDocument
     */
    private VectorDocument prepareVectorDocument(RepositoryMemory repositoryMemory, ChunkFile chunkFile, String filePath, Integer totalChunks) {
        return VectorDocument.builder()
                .id(idService.newId())
                .text(chunkFile.getText())
                .metadata(Map.of(
                        "repositoryId", repositoryMemory.getRepositoryId(),
                        "source", filePath,
                        "index", chunkFile.getIndex(),
                        "startLine", chunkFile.getStartLine(),
                        "endLine", chunkFile.getEndLine(),
                        "totalChunks", totalChunks
                ))
                .build();
    }

    // Query methods (non-reactive)
    public List<VectorDocument> overviewQuery(QueryRequest request, User user) {
        log.debug("Running overviewQuery: {}", request);
        return overviewRepository.query(request);
    }

    public List<VectorDocument> chunkQuery(QueryRequest request, User user) {
        log.debug("Running chunkQuery: {}", request);
        return chunkRepository.query(request);
    }
}
