package ir.msob.manak.memory.documentmemory;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;
import ir.msob.jima.crud.api.restful.client.domain.DomainCrudWebClient;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.service.jima.service.IdService;
import ir.msob.manak.domain.model.dms.document.DocumentDto;
import ir.msob.manak.domain.model.memory.documentmemory.DocumentMemory;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.domain.model.util.chunk.ChunkFile;
import ir.msob.manak.domain.model.util.chunk.Chunker;
import ir.msob.manak.domain.service.client.FileClient;
import ir.msob.manak.domain.service.properties.ManakProperties;
import ir.msob.manak.memory.common.SummarizerService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DocumentMemoryService {

    private static final Logger log = LoggerFactory.getLogger(DocumentMemoryService.class);

    private final FileClient fileClient;
    private final DomainCrudWebClient domainCrudWebClient;
    private final DocumentOverviewMemoryVectorRepository overviewRepository;
    private final DocumentChunkMemoryVectorRepository chunkRepository;
    private final ManakProperties manakProperties;
    private final IdService idService;
    private final SummarizerService summarizer;

    /**
     * Main entrypoint: loads document info, downloads file, chunks & indexes.
     */
    public Mono<DocumentMemory> save(DocumentMemory dto, User user) {
        log.info("Start saving document memory. documentId={}", dto.getDocumentId());

        return domainCrudWebClient.getById(DocumentDto.class, dto.getDocumentId(), user)
                .switchIfEmpty(Mono.error(new IllegalStateException("Document not found: " + dto.getDocumentId())))
                .map(this::prepareDocumentMemory)
                .flatMap(documentMemory ->
                        downloadAndIndex(documentMemory, user)
                                .thenReturn(documentMemory)
                )
                .doOnSuccess(result ->
                        log.info("Document memory saved successfully. documentId={}", result.getDocumentId()))
                .doOnError(error ->
                        log.error("Error while saving document memory. documentId={}, error={}",
                                dto.getDocumentId(), error.getMessage(), error)
                );
    }

    private Mono<Void> downloadAndIndex(DocumentMemory documentMemory, User user) {
        log.debug("Downloading file for documentId={} path={}",
                documentMemory.getDocumentId(), documentMemory.getFilePath());

        return fileClient.downloadFile(documentMemory.getFilePath(), user)
                .flatMap(inputStream -> index(documentMemory, inputStream))
                .doOnError(e ->
                        log.error("Failed to download or index file: {}", documentMemory.getFilePath(), e)
                );
    }


    /**
     * Convert DocumentDto → DocumentMemory
     */
    private DocumentMemory prepareDocumentMemory(DocumentDto document) {
        return document.getLatestAttachment()
                .map(a -> {
                    log.debug("Preparing DocumentMemory for documentId={} file={}",
                            document.getId(), a.getFileName());
                    return DocumentMemory.builder()
                            .documentId(document.getId())
                            .filePath(a.getFilePath())
                            .fileName(a.getFileName())
                            .mimeType(a.getMimeType())
                            .build();
                })
                .orElseThrow(() -> new IllegalStateException("No attachment found for document " + document.getId()));
    }


    /**
     * Index chunks + overview
     */
    private Mono<Void> index(DocumentMemory documentMemory, InputStreamResource fileContent) {
        log.info("Indexing chunks for file {} (documentId={})",
                documentMemory.getFileName(), documentMemory.getDocumentId());

        List<VectorDocument> chunks = chunk(documentMemory, fileContent);

        log.debug("Generated {} chunks for documentId={}", chunks.size(), documentMemory.getDocumentId());

        VectorDocument overview = generateOverview(documentMemory, chunks);

        log.debug("Generated overview vector. documentId={}", documentMemory.getDocumentId());

        // Repo operations are synchronous; wrap in Mono
        return Mono.fromRunnable(() -> {
            chunkRepository.save(chunks);
            overviewRepository.save(overview);
        }).then();
    }


    /**
     * Chunk file content using Chunker
     */
    public List<VectorDocument> chunk(DocumentMemory documentMemory, InputStreamResource contentBytes) {
        log.debug("Chunking documentId={} with chunkSize={} and overlap={}",
                documentMemory.getDocumentId(),
                manakProperties.getMemory().getChunk().getChunkSize(),
                manakProperties.getMemory().getChunk().getOverlap());

        List<ChunkFile> chunkFiles = Chunker.chunk(
                contentBytes,
                Chunker.FileType.MARKDOWN,
                manakProperties.getMemory().getChunk().getChunkSize(),
                manakProperties.getMemory().getChunk().getOverlap()
        );

        return chunkFiles
                .stream()
                .map(chunk -> prepareVectorDocument(documentMemory, chunk, chunkFiles.size()))
                .toList();
    }


    /**
     * Convert ChunkFile → VectorDocument
     */
    private VectorDocument prepareVectorDocument(DocumentMemory doc, ChunkFile chunkFile, Integer totalChunks) {
        return VectorDocument.builder()
                .id(idService.newId())
                .text(chunkFile.getText())
                .metadata(Map.of(
                        "documentId", doc.getDocumentId(),
                        "source", doc.getFilePath(),
                        "index", chunkFile.getIndex(),
                        "startLine", chunkFile.getStartLine(),
                        "endLine", chunkFile.getEndLine(),
                        "totalChunks", totalChunks
                ))
                .build();
    }

    /**
     * Create Overview VectorDocument
     */
    public VectorDocument generateOverview(DocumentMemory documentMemory, List<VectorDocument> chunks) {
        List<String> texts = chunks.stream().map(VectorDocument::getText).collect(Collectors.toList());
        String overviewText = summarizer.summarize(texts);
        return VectorDocument.builder()
                .id(idService.newId())
                .text(overviewText)
                .metadata(java.util.Map.of(
                        "documentId", documentMemory.getDocumentId(),
                        "source", documentMemory.getFilePath()
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
