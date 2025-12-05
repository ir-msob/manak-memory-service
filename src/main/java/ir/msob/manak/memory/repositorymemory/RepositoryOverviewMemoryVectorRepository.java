package ir.msob.manak.memory.repositorymemory;

import ir.msob.manak.memory.vectordb.VectorCrudRepository;
import ir.msob.manak.memory.vectordb.VectorStoreFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RepositoryOverviewMemoryVectorRepository extends VectorCrudRepository {

    protected RepositoryOverviewMemoryVectorRepository(VectorStoreFactory vectorStoreFactory) {
        super(vectorStoreFactory);
    }

    @Override
    protected String getCollectionName() {
        return "RepositoryOverview";
    }

}