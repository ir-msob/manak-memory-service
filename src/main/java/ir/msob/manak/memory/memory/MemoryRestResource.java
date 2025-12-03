package ir.msob.manak.memory.memory;

import ir.msob.jima.core.commons.operation.ConditionalOnOperation;
import ir.msob.jima.core.commons.resource.Resource;
import ir.msob.jima.core.commons.shared.ResourceType;
import ir.msob.manak.core.service.jima.crud.restful.domain.service.DomainCrudRestResource;
import ir.msob.manak.core.service.jima.security.UserService;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static ir.msob.jima.core.commons.operation.Operations.*;

@RestController
@RequestMapping(MemoryRestResource.BASE_URI)
@ConditionalOnOperation(operations = {SAVE, UPDATE_BY_ID, DELETE_BY_ID, EDIT_BY_ID, GET_BY_ID, GET_PAGE})
@Resource(value = Memory.DOMAIN_NAME_WITH_HYPHEN, type = ResourceType.RESTFUL)
public class MemoryRestResource extends DomainCrudRestResource<Memory, MemoryDto, MemoryCriteria, MemoryRepository, MemoryService> {
    public static final String BASE_URI = "/api/v1/" +Memory.DOMAIN_NAME_WITH_HYPHEN;

    protected MemoryRestResource(UserService userService, MemoryService service) {
        super(userService, service);
    }
}
