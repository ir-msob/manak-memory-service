package ir.msob.manak.memory.memory;

import ir.msob.manak.domain.model.memory.memory.Memory;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.memory.vectordb.VectorDomainCrudRepository;
import ir.msob.manak.memory.vectordb.VectorStoreFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class MemoryVectorRepository extends VectorDomainCrudRepository<Memory> {

    protected MemoryVectorRepository(VectorStoreFactory vectorStoreFactory) {
        super(vectorStoreFactory);
    }

    public List<VectorDocument> query(QueryRequest query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
        return super.query(query.getQuery(), query.getTopK(), query.getSimilarityThreshold(), query.getMetadata());
    }
}

