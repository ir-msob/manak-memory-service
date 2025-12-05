package ir.msob.manak.memory.documentmemory;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.service.jima.security.UserService;
import ir.msob.manak.domain.model.memory.documentmemory.DocumentMemory;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/v1/document-memory")
@RequiredArgsConstructor
public class DocumentMemoryController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final DocumentMemoryService service;
    private final UserService userService;

    @PostMapping
    public Mono<DocumentMemory> save(@RequestBody DocumentMemory dto, Principal principal) {
        logger.debug("REST request to add document-memory, documentId: {}", dto.getDocumentId());
        User user = userService.getUser(principal);
        return service.save(dto, user);
    }

    @PostMapping("/overview/query")
    public List<VectorDocument> overviewQuery(@RequestBody QueryRequest query, Principal principal) {
        logger.debug("REST request to document overview-query, query: {}, topK: {}", query.getQuery(), query.getTopK());
        User user = userService.getUser(principal);
        return service.overviewQuery(query, user);
    }

    @PostMapping("/chunk/query")
    public List<VectorDocument> chunkQuery(@RequestBody QueryRequest query, Principal principal) {
        logger.debug("REST request to document chunk-query, query: {}, topK: {}", query.getQuery(), query.getTopK());
        User user = userService.getUser(principal);
        return service.chunkQuery(query, user);
    }
}
