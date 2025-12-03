package ir.msob.manak.memory.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.github.fge.jackson.jsonpointer.JsonPointer;
import com.github.fge.jackson.jsonpointer.JsonPointerException;
import com.github.fge.jsonpatch.JsonPatch;
import com.github.fge.jsonpatch.JsonPatchOperation;
import com.github.fge.jsonpatch.ReplaceOperation;
import ir.msob.jima.core.commons.id.BaseIdService;
import org.assertj.core.api.Assertions;
import ir.msob.manak.core.test.jima.crud.base.domain.DomainCrudDataProvider;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import lombok.SneakyThrows;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static ir.msob.jima.core.test.CoreTestData.DEFAULT_STRING;
import static ir.msob.jima.core.test.CoreTestData.UPDATED_STRING;

/**
 * This class provides test data for the {@link Memory} class. It extends the {@link DomainCrudDataProvider} class
 * and provides methods to create new test data objects, update existing data objects, and generate JSON patches for updates.
 */
@Component
public class MemoryDataProvider extends DomainCrudDataProvider<Memory, MemoryDto, MemoryCriteria, MemoryRepository, MemoryService> {

    protected MemoryDataProvider(BaseIdService idService, ObjectMapper objectMapper, MemoryService service) {
        super(idService, objectMapper, service);
    }

    private static MemoryDto newDto;
    private static MemoryDto newMandatoryDto;

    /**
     * Creates a new DTO object with default values.
     */
    public static void createNewDto() {
        newDto = prepareMandatoryDto();
        newDto.setDescription(DEFAULT_STRING);
    }

    /**
     * Creates a new DTO object with mandatory fields set.
     */
    public static void createMandatoryNewDto() {
        newMandatoryDto = prepareMandatoryDto();
    }

    /**
     * Creates a new DTO object with mandatory fields set.
     */
    public static MemoryDto prepareMandatoryDto() {
        MemoryDto dto = new MemoryDto();
        dto.setTitle(DEFAULT_STRING);
        return dto;
    }

    /**
     */
    @Override
    @SneakyThrows
    public JsonPatch getJsonPatch() {
        List<JsonPatchOperation> operations = getMandatoryJsonPatchOperation();
        operations.add(new ReplaceOperation(new JsonPointer(String.format("/%s", Memory.FN.description)), new TextNode(UPDATED_STRING)));
        return new JsonPatch(operations);
    }

    /**
     */
    @Override
    @SneakyThrows
    public JsonPatch getMandatoryJsonPatch() {
        return new JsonPatch(getMandatoryJsonPatchOperation());
    }

    /**
     *
     */
    @Override
    public MemoryDto getNewDto() {
        return newDto;
    }

    /**
     * Updates the given DTO object with the updated value for the domain field.
     *
     * @param dto the DTO object to update
     */
    @Override
    public void updateDto(MemoryDto dto) {
        updateMandatoryDto(dto);
        dto.setDescription(UPDATED_STRING);
    }

    /**
     *
     */
    @Override
    public MemoryDto getMandatoryNewDto() {
        return newMandatoryDto;
    }

    /**
     * Updates the given DTO object with the updated value for the mandatory field.
     *
     * @param dto the DTO object to update
     */
    @Override
    public void updateMandatoryDto(MemoryDto dto) {
        dto.setTitle(UPDATED_STRING);
    }

    /**
     * Creates a list of JSON patch operations for updating the mandatory field.
     *
     * @return a list of JSON patch operations
     * @throws JsonPointerException if there is an error creating the JSON pointer.
     */
    public List<JsonPatchOperation> getMandatoryJsonPatchOperation() throws JsonPointerException {
        List<JsonPatchOperation> operations = new ArrayList<>();
        operations.add(new ReplaceOperation(new JsonPointer(String.format("/%s", Memory.FN.name)), new TextNode(UPDATED_STRING)));
        return operations;
    }

    @Override
    public void assertMandatoryGet(MemoryDto before, MemoryDto after) {
        super.assertMandatoryGet(before, after);
        Assertions.assertThat(after.getTitle()).isEqualTo(before.getTitle());
    }

    @Override
    public void assertGet(MemoryDto before, MemoryDto after) {
        super.assertGet(before, after);
        assertMandatoryGet(before, after);

        Assertions.assertThat(after.getDescription()).isEqualTo(before.getDescription());
    }

    @Override
    public void assertMandatoryUpdate(MemoryDto dto, MemoryDto updatedDto) {
        super.assertMandatoryUpdate(dto, updatedDto);
        Assertions.assertThat(dto.getTitle()).isEqualTo(DEFAULT_STRING);
        Assertions.assertThat(updatedDto.getTitle()).isEqualTo(UPDATED_STRING);
    }

    @Override
    public void assertUpdate(MemoryDto dto, MemoryDto updatedDto) {
        super.assertUpdate(dto, updatedDto);
        assertMandatoryUpdate(dto, updatedDto);
    }

    @Override
    public void assertMandatorySave(MemoryDto dto, MemoryDto savedDto) {
        super.assertMandatorySave(dto, savedDto);
        assertMandatoryGet(dto, savedDto);
    }

    @Override
    public void assertSave(MemoryDto dto, MemoryDto savedDto) {
        super.assertSave(dto, savedDto);
        assertGet(dto, savedDto);
    }
}