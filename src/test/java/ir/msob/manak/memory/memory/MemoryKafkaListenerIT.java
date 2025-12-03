package ir.msob.manak.memory.memory;

import ir.msob.jima.core.commons.resource.BaseResource;
import ir.msob.jima.core.test.CoreTestData;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.test.jima.crud.kafka.domain.DomainCrudKafkaListenerTest;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import ir.msob.manak.domain.model.memory.memory.MemoryTypeReference;
import ir.msob.manak.memory.Application;
import ir.msob.manak.memory.ContainerConfiguration;
import lombok.SneakyThrows;
import lombok.extern.apachecommons.CommonsLog;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest(classes = {Application.class, ContainerConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@CommonsLog
public class MemoryKafkaListenerIT
        extends DomainCrudKafkaListenerTest<Memory, MemoryDto, MemoryCriteria, MemoryRepository, MemoryService, MemoryDataProvider>
        implements MemoryTypeReference {

    @SneakyThrows
    @BeforeAll
    public static void beforeAll() {
        CoreTestData.init(new ObjectId(), new ObjectId());
    }

    @SneakyThrows
    @BeforeEach
    public void beforeEach() {
        getDataProvider().cleanups();
        MemoryDataProvider.createMandatoryNewDto();
        MemoryDataProvider.createNewDto();
    }

    @Override
    public Class<? extends BaseResource<String, User>> getResourceClass() {
        return MemoryKafkaListener.class;
    }

    @Override
    public String getBaseUri() {
        return MemoryKafkaListener.BASE_URI;
    }
}
