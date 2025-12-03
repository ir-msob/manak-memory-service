package ir.msob.manak.memory.memory;

import ir.msob.jima.core.commons.exception.badrequest.BadRequestException;
import ir.msob.jima.core.commons.exception.domainnotfound.DomainNotFoundException;
import ir.msob.manak.core.model.jima.operation.BeforeAfterDomainOperation;
import ir.msob.manak.core.model.jima.security.User;
import ir.msob.manak.domain.model.memory.memory.MemoryCriteria;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VectorStorageBeforeAfter implements BeforeAfterDomainOperation<MemoryDto, MemoryCriteria> {
    @Override
    public void afterSave(MemoryDto dto, MemoryDto savedDto, User user) throws DomainNotFoundException, BadRequestException {
        BeforeAfterDomainOperation.super.afterSave(dto, savedDto, user);
    }

    @Override
    public void afterUpdate(MemoryDto previousDto, MemoryDto updatedDto, User user) throws DomainNotFoundException, BadRequestException {
        BeforeAfterDomainOperation.super.afterUpdate(previousDto, updatedDto, user);
    }

    @Override
    public void afterDelete(MemoryDto dto, MemoryCriteria criteria, User user) throws DomainNotFoundException, BadRequestException {
        BeforeAfterDomainOperation.super.afterDelete(dto, criteria, user);
    }
}
