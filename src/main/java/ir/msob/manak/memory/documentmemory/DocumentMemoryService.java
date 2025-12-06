package ir.msob.manak.memory.documentmemory;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;
import ir.msob.jima.crud.api.restful.client.domain.DomainCrudWebClient;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.domain.model.dms.document.DocumentDto;
import ir.msob.manak.domain.model.memory.documentmemory.DocumentMemory;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.domain.model.util.chunk.ChunkFile;
import ir.msob.manak.domain.model.util.chunk.Chunker;
import ir.msob.manak.domain.service.client.FileClient;
import ir.msob.manak.domain.service.properties.ManakProperties;
import ir.msob.manak.memory.common.ChunkService;
import ir.msob.manak.memory.common.OverviewGenerator;
import ir.msob.manak.memory.common.VectorDocumentFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DocumentMemoryService {

    private static final Logger log = LoggerFactory.getLogger(DocumentMemoryService.class);

    private final FileClient fileClient;
    private final DomainCrudWebClient domainCrudWebClient;
    private final ir.msob.manak.memory.documentmemory.DocumentOverviewMemoryVectorRepository overviewRepository;
    private final ir.msob.manak.memory.documentmemory.DocumentChunkMemoryVectorRepository chunkRepository;
    private final ManakProperties manakProperties;
    private final ChunkService chunkService;
    private final VectorDocumentFactory vectorFactory;
    private final OverviewGenerator overviewGenerator;

    public Mono<DocumentMemory> save(DocumentMemory dto, User user) {
        log.info("Start saving document memory documentId={}", dto.getDocumentId());
        return domainCrudWebClient.getById(DocumentDto.class, dto.getDocumentId(), user)
                .switchIfEmpty(Mono.error(new IllegalStateException("Document not found: " + dto.getDocumentId())))
                .map(this::toDocumentMemory)
                .flatMap(dm -> downloadAndIndex(dm, user).thenReturn(dm))
                .doOnSuccess(d -> log.info("Document memory saved documentId={}", d.getDocumentId()))
                .doOnError(e -> log.error("Failed saving document memory documentId={}: {}", dto.getDocumentId(), e.getMessage(), e));
    }

    public Flux<VectorDocument> overviewQuery(QueryRequest request, User user) {
        log.debug("overviewQuery {}", request);
        return Flux.fromIterable(overviewRepository.query(request));
    }

    public Flux<VectorDocument> chunkQuery(QueryRequest request, User user) {
        log.debug("chunkQuery {}", request);
        return Flux.fromIterable(chunkRepository.query(request));
    }


    private DocumentMemory toDocumentMemory(DocumentDto document) {
        return document.getLatestAttachment()
                .map(a -> DocumentMemory.builder()
                        .documentId(document.getId())
                        .filePath(a.getFilePath())
                        .fileName(a.getFileName())
                        .mimeType(a.getMimeType())
                        .build())
                .orElseThrow(() -> new IllegalStateException("No attachment for document " + document.getId()));
    }

    private Mono<Void> downloadAndIndex(DocumentMemory dm, User user) {
        log.debug("Downloading file path={} documentId={}", dm.getFilePath(), dm.getDocumentId());
        return fileClient.downloadFile(dm.getFilePath(), user)
                .flatMap(isr -> index(dm, isr))
                .doOnError(e -> log.error("Download/index failed for {}: {}", dm.getFilePath(), e.getMessage(), e));
    }

    private Mono<Void> index(DocumentMemory dm, InputStreamResource content) {
        log.info("Indexing document {} ({})", dm.getDocumentId(), dm.getFileName());

        List<ChunkFile> chunkFiles = chunkService.chunkInputStream(content, Chunker.FileType.MARKDOWN);
        List<VectorDocument> chunks = vectorFactory.fromChunks("documentId", dm.getDocumentId(), dm.getFilePath(), chunkFiles);

        log.debug("Generated {} chunks for documentId={}", chunks.size(), dm.getDocumentId());

        VectorDocument overview = overviewGenerator.generate("documentId", dm.getDocumentId(), dm.getFilePath(), chunks);
        log.debug("Generated overview for documentId={}", dm.getDocumentId());

        return Mono.fromRunnable(() -> {
            chunkRepository.save(chunks);
            overviewRepository.save(overview);
        }).then();
    }
}