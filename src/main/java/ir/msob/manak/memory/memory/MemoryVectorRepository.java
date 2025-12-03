package ir.msob.manak.memory.memory;

import ir.msob.jima.core.commons.filter.Param;
import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.model.MemoryQuery;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.memory.vectordb.VectorCrudRepository;
import ir.msob.manak.memory.vectordb.VectorStoreFactory;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

@Repository
public class MemoryVectorRepository extends VectorCrudRepository<Memory> {

    protected MemoryVectorRepository(VectorStoreFactory vectorStoreFactory) {
        super(vectorStoreFactory);
    }

    public List<VectorDocument> query(MemoryQuery memoryQuery) {
        if (memoryQuery == null) {
            throw new IllegalArgumentException("query must not be null");
        }

        Map<String, Param<?>> paramMap = Map.of(
                Memory.FN.type.name(), memoryQuery.getType(),
                Memory.FN.tags.name(), memoryQuery.getTags(),
                Memory.FN.scopes.name(), memoryQuery.getScopes(),
                Memory.FN.validityScope.name(), memoryQuery.getValidityScope(),
                Memory.FN.priority.name(), memoryQuery.getPriority(),
                Memory.FN.sourceType.name(), memoryQuery.getSourceType(),
                MemoryQuery.FN.sourceName.name(), memoryQuery.getSourceName(),
                MemoryQuery.FN.sourceId.name(), memoryQuery.getSourceId(),
                MemoryQuery.FN.sourceReferringType.name(), memoryQuery.getSourceReferringType(),
                MemoryQuery.FN.sourceRelatedId.name(), memoryQuery.getSourceRelatedId()
        );

        return super.query(memoryQuery.getQuery(), memoryQuery.getTopK(), memoryQuery.getSimilarityThreshold(), paramMap);
    }
}

