package ir.msob.manak.memory.documentmemory;

import ir.msob.manak.memory.vectordb.VectorCrudRepository;
import ir.msob.manak.memory.vectordb.VectorStoreFactory;
import org.springframework.stereotype.Repository;

@Repository
public class DocumentOverviewMemoryVectorRepository extends VectorCrudRepository {

    protected DocumentOverviewMemoryVectorRepository(VectorStoreFactory vectorStoreFactory) {
        super(vectorStoreFactory);
    }

    @Override
    protected String getCollectionName() {
        return "DocumentOverview";
    }

}