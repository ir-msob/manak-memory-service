package ir.msob.manak.memory.memory;

import ir.msob.manak.core.test.jima.crud.base.childdomain.characteristic.BaseCharacteristicCrudDataProvider;
import ir.msob.manak.domain.model.memory.memory.MemoryDto;
import org.springframework.stereotype.Component;

@Component
public class MemoryCharacteristicCrudDataProvider extends BaseCharacteristicCrudDataProvider<MemoryDto, MemoryService> {
    public MemoryCharacteristicCrudDataProvider(MemoryService childService) {
        super(childService);
    }
}
