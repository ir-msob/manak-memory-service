package ir.msob.manak.memory.memory;

import ir.msob.jima.core.commons.exception.badrequest.BadRequestException;
import ir.msob.jima.core.commons.exception.domainnotfound.DomainNotFoundException;
import ir.msob.manak.core.model.jima.operation.BeforeAfterDomainOperation;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.util.Strings;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class MemoryVectorBeforeAfter implements BeforeAfterDomainOperation<MemoryDto, MemoryCriteria> {
    private final MemoryVectorRepository memoryVectorRepository;

    @Override
    public void afterSave(MemoryDto dto, MemoryDto savedDto, User user) throws DomainNotFoundException, BadRequestException {
        if (savedDto.getStatus() == Memory.MemoryStatus.ACTIVE) {
            VectorDocument vectorDocument = prepareVectorDocument(savedDto);
            memoryVectorRepository.save(vectorDocument);
        }
        BeforeAfterDomainOperation.super.afterSave(dto, savedDto, user);
    }

    @Override
    public void afterUpdate(MemoryDto previousDto, MemoryDto updatedDto, User user) throws DomainNotFoundException, BadRequestException {
        if (updatedDto.getStatus() == Memory.MemoryStatus.ACTIVE) {
            VectorDocument vectorDocument = prepareVectorDocument(updatedDto);
            memoryVectorRepository.update(vectorDocument);
        } else {
            memoryVectorRepository.delete(updatedDto.getId());
        }
        BeforeAfterDomainOperation.super.afterUpdate(previousDto, updatedDto, user);
    }

    @Override
    public void afterDelete(MemoryDto dto, MemoryCriteria criteria, User user) throws DomainNotFoundException, BadRequestException {
        memoryVectorRepository.delete(dto.getId());
        BeforeAfterDomainOperation.super.afterDelete(dto, criteria, user);
    }

    private VectorDocument prepareVectorDocument(MemoryDto dto) {
        Map<String, String> metadata = dto.getRelatedDomains().stream()
                .filter(rd -> "Source".equalsIgnoreCase(rd.getRole()))
                .findFirst()
                .map(rd -> {
                    Map<String, String> map = new HashMap<>();
                    if (Strings.isNotBlank(rd.getName())) map.put(FN.sourceName.name(), rd.getName());
                    if (Strings.isNotBlank(rd.getRelatedId())) map.put(FN.sourceId.name(), rd.getRelatedId());
                    if (Strings.isNotBlank(rd.getReferringType()))
                        map.put(FN.sourceReferringType.name(), rd.getReferringType());
                    if (Strings.isNotBlank(rd.getRelatedId())) map.put(FN.sourceRelatedId.name(), rd.getRelatedId());
                    return map;
                })
                .orElse(new HashMap<>());


        return VectorDocument.builder()
                .id(dto.getId())
                .text(dto.getDescription())
                .metadata(metadata)
                .metaEntry(Memory.FN.type.name(), dto.getType().name())
                .metaEntry(Memory.FN.tags.name(), dto.getTags())
                .metaEntry(Memory.FN.scopes.name(), dto.getScopes())
                .metaEntry(Memory.FN.validityScope.name(), dto.getValidityScope())
                .metaEntry(Memory.FN.priority.name(), dto.getPriority())
                .metaEntry(Memory.FN.sourceType.name(), dto.getSourceType())
                .build();
    }

    public enum FN {
        sourceName,
        sourceId,
        sourceReferringType,
        sourceRelatedId
    }
}