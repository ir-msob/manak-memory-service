package ir.msob.manak.memory.repositorymemory;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.service.jima.security.UserService;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.domain.model.memory.repositorymemory.RepositoryMemory;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.security.Principal;

@RestController
@RequestMapping("/api/v1/repository-memory")
@RequiredArgsConstructor
public class RepositoryMemoryController {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final RepositoryMemoryService service;
    private final UserService userService;

    @PostMapping
    public ResponseEntity<Mono<RepositoryMemory>> save(@RequestBody RepositoryMemory dto, Principal principal) {
        logger.debug("REST request to add repository-memory, repositoryId: {}", dto.getRepositoryId());
        User user = userService.getUser(principal);
        Mono<RepositoryMemory> res = service.save(dto, user);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/overview/query")
    public ResponseEntity<Flux<VectorDocument>> overviewQuery(@RequestBody QueryRequest query, Principal principal) {
        logger.debug("REST request to repository overview-query, query: {}, topK: {}", query.getQuery(), query.getTopK());
        User user = userService.getUser(principal);
        Flux<VectorDocument> res = service.overviewQuery(query, user);
        return ResponseEntity.ok(res);
    }

    @PostMapping("/chunk/query")
    public ResponseEntity<Flux<VectorDocument>> chunkQuery(@RequestBody QueryRequest query, Principal principal) {
        logger.debug("REST request to repository chunk-query, query: {}, topK: {}", query.getQuery(), query.getTopK());
        User user = userService.getUser(principal);
        Flux<VectorDocument> res = service.chunkQuery(query, user);
        return ResponseEntity.ok(res);
    }
}
