package ir.msob.manak.memory.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.msob.jima.core.commons.client.BaseAsyncClient;
import ir.msob.jima.core.commons.operation.ConditionalOnOperation;
import ir.msob.jima.core.commons.resource.Resource;
import ir.msob.jima.core.commons.shared.ResourceType;
import ir.msob.jima.crud.api.kafka.client.ChannelUtil;
import ir.msob.manak.core.service.jima.crud.kafka.domain.service.DomainCrudKafkaListener;
import ir.msob.manak.core.service.jima.security.UserService;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import ir.msob.manak.domain.model.memory.memory.MemoryTypeReference;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.stereotype.Component;

import static ir.msob.jima.core.commons.operation.Operations.*;

@Component
@ConditionalOnOperation(operations = {SAVE, UPDATE_BY_ID, DELETE_BY_ID})
@Resource(value = Memory.DOMAIN_NAME_WITH_HYPHEN, type = ResourceType.KAFKA)
public class MemoryKafkaListener
        extends DomainCrudKafkaListener<Memory, MemoryDto, MemoryCriteria, MemoryRepository, MemoryService>
        implements MemoryTypeReference {
    public static final String BASE_URI = ChannelUtil.getBaseChannel(MemoryDto.class);

    protected MemoryKafkaListener(UserService userService, MemoryService service, ObjectMapper objectMapper, ConsumerFactory<String, String> consumerFactory, BaseAsyncClient asyncClient) {
        super(userService, service, objectMapper, consumerFactory, asyncClient);
    }

}
