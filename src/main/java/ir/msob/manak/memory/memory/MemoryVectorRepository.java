package ir.msob.manak.memory.memory;

import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.memory.vectordb.VectorCrudRepository;
import ir.msob.manak.memory.vectordb.VectorStoreBuilder;
import lombok.Getter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Repository;

@Repository
public class MemoryVectorRepository implements VectorCrudRepository {

    @Getter
    private final VectorStore vectorStore;

    public MemoryVectorRepository(VectorStoreBuilder vectorStoreBuilder) {
        this.vectorStore = vectorStoreBuilder.buildVectorStore(Memory.DOMAIN_NAME);
    }
}

