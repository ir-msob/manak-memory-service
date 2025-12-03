package ir.msob.manak.memory.memory;

import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;
import ir.msob.jima.core.commons.exception.badrequest.BadRequestException;
import ir.msob.jima.core.commons.exception.badrequest.BadRequestResponse;
import ir.msob.jima.core.commons.exception.domainnotfound.DomainNotFoundException;
import ir.msob.jima.core.commons.methodstats.MethodStats;
import ir.msob.jima.core.commons.operation.ConditionalOnOperation;
import ir.msob.jima.core.commons.resource.Resource;
import ir.msob.jima.core.commons.scope.Scope;
import ir.msob.jima.core.commons.shared.ResourceType;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.service.jima.crud.restful.domain.service.DomainCrudRestResource;
import ir.msob.manak.core.service.jima.security.UserService;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import ir.msob.manak.domain.model.memory.model.MemoryQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.security.Principal;

import static ir.msob.jima.core.commons.operation.Operations.*;

@RestController
@RequestMapping(MemoryRestResource.BASE_URI)
@ConditionalOnOperation(operations = {SAVE, UPDATE_BY_ID, DELETE_BY_ID, EDIT_BY_ID, GET_BY_ID, GET_PAGE})
@Resource(value = Memory.DOMAIN_NAME_WITH_HYPHEN, type = ResourceType.RESTFUL)
public class MemoryRestResource extends DomainCrudRestResource<Memory, MemoryDto, MemoryCriteria, MemoryRepository, MemoryService> {
    public static final String BASE_URI = "/api/v1/" + Memory.DOMAIN_NAME_WITH_HYPHEN;
    private final UserService userService;
    Logger log = LoggerFactory.getLogger(MemoryRestResource.class);

    protected MemoryRestResource(UserService userService, MemoryService service, UserService userService1) {
        super(userService, service);
        this.userService = userService1;
    }


    @PostMapping("query")
    @ApiResponses(value = {
            @ApiResponse(code = 200, message = "Return a domains or empty"),
            @ApiResponse(code = 400, message = "Bad request", response = BadRequestResponse.class),
            @ApiResponse(code = 404, message = "Domain not found", response = DomainNotFoundException.class)
    })
    @MethodStats
    @Scope(operation = "query")
    public ResponseEntity<Flux<MemoryDto>> query(@RequestBody MemoryQuery query, Principal principal) throws BadRequestException, DomainNotFoundException {
        log.debug("REST request to query , query {}, topK : {}", query.getQuery(), query.getTopK());
        User user = userService.getUser(principal);
        Flux<MemoryDto> res = this.getService().query(query, user);
        return ResponseEntity.ok(res);
    }
}
