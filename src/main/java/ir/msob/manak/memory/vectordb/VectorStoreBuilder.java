package ir.msob.manak.memory.vectordb;

import io.milvus.client.MilvusServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorStoreBuilder {
    private final MilvusServiceClient milvusServiceClient;
    private final EmbeddingModel embeddingMode;

    @SneakyThrows
    public VectorStore buildVectorStore(String collectionName) {
        MilvusVectorStore vectorStore = MilvusVectorStore.builder(milvusServiceClient, embeddingMode)
                .collectionName(collectionName)
                .build();
        vectorStore.afterPropertiesSet();
        return vectorStore;
    }
}
