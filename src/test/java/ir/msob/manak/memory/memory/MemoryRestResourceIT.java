package ir.msob.manak.memory.memory;

import ir.msob.jima.core.commons.resource.BaseResource;
import ir.msob.jima.core.test.CoreTestData;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.test.jima.crud.restful.domain.DomainCrudRestResourceTest;
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
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.testcontainers.junit.jupiter.Testcontainers;

@AutoConfigureWebTestClient
@SpringBootTest(classes = {Application.class, ContainerConfiguration.class}, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@CommonsLog
public class MemoryRestResourceIT
        extends DomainCrudRestResourceTest<Memory, MemoryDto, MemoryCriteria, MemoryRepository, MemoryService, MemoryDataProvider>
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
    public String getBaseUri() {
        return MemoryRestResource.BASE_URI;
    }

    @Override
    public Class<? extends BaseResource<String, User>> getResourceClass() {
        return MemoryRestResource.class;
    }

}
