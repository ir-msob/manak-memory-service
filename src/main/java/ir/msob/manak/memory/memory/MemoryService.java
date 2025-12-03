package ir.msob.manak.memory.memory;

import com.fasterxml.jackson.databind.ObjectMapper;
import ir.msob.jima.core.commons.id.BaseIdService;
import ir.msob.jima.core.commons.operation.BaseBeforeAfterDomainOperation;
import ir.msob.jima.crud.service.domain.BeforeAfterComponent;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.core.service.jima.crud.base.childdomain.ChildDomainCrudService;
import ir.msob.manak.core.service.jima.crud.base.domain.DomainCrudService;
import ir.msob.manak.core.service.jima.service.IdService;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import ir.msob.manak.domain.model.memory.model.MemoryQuery;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import jakarta.validation.Valid;
import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Service
public class MemoryService extends DomainCrudService<Memory, MemoryDto, MemoryCriteria, MemoryRepository>
        implements ChildDomainCrudService<MemoryDto> {

    private final ModelMapper modelMapper;
    private final IdService idService;
    private final MemoryVectorRepository memoryVectorRepository;

    protected MemoryService(BeforeAfterComponent beforeAfterComponent, ObjectMapper objectMapper, MemoryRepository repository, ModelMapper modelMapper, IdService idService, MemoryVectorRepository memoryVectorRepository) {
        super(beforeAfterComponent, objectMapper, repository);
        this.modelMapper = modelMapper;
        this.idService = idService;
        this.memoryVectorRepository = memoryVectorRepository;
    }

    @Override
    public MemoryDto toDto(Memory domain, User user) {
        return modelMapper.map(domain, MemoryDto.class);
    }

    @Override
    public Memory toDomain(MemoryDto dto, User user) {
        return dto;
    }

    @Override
    public Collection<BaseBeforeAfterDomainOperation<String, User, MemoryDto, MemoryCriteria>> getBeforeAfterDomainOperations() {
        return Collections.emptyList();
    }

    @Transactional(readOnly = true)
    @Override
    public Mono<MemoryDto> getDto(String id, User user) {
        return super.getOne(id, user);
    }

    @Transactional
    @Override
    public Mono<MemoryDto> updateDto(String id, @Valid MemoryDto dto, User user) {
        return super.update(id, dto, user);
    }

    @Override
    public BaseIdService getIdService() {
        return idService;
    }

    @Transactional(readOnly = true)
    public Flux<MemoryDto> query(MemoryQuery query, User user) {
        List<VectorDocument> vectorDocuments = memoryVectorRepository.query(query);
        List<String> ids = vectorDocuments.stream()
                .map(VectorDocument::getId)
                .toList();
        return getStream(ids, user);
    }
}
