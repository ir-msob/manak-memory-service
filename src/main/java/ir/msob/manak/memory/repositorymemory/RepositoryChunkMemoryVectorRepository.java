package ir.msob.manak.memory.repositorymemory;

import ir.msob.manak.memory.vectordb.VectorCrudRepository;
import ir.msob.manak.memory.vectordb.VectorStoreFactory;
import org.springframework.stereotype.Repository;

@Repository
public class RepositoryChunkMemoryVectorRepository extends VectorCrudRepository {

    protected RepositoryChunkMemoryVectorRepository(VectorStoreFactory vectorStoreFactory) {
        super(vectorStoreFactory);
    }

    @Override
    protected String getCollectionName() {
        return "RepositoryChunk";
    }

}