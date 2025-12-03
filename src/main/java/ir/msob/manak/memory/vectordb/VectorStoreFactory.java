package ir.msob.manak.memory.vectordb;

import io.milvus.client.MilvusServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.milvus.MilvusVectorStore;
import org.springframework.ai.vectorstore.milvus.autoconfigure.MilvusVectorStoreProperties;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class VectorStoreFactory {
    private final MilvusServiceClient milvusServiceClient;
    private final EmbeddingModel embeddingModel;
    private final MilvusVectorStoreProperties properties;

    @SneakyThrows
    public VectorStore buildVectorStore(String collectionName) {
        MilvusVectorStore vectorStore = MilvusVectorStore.builder(milvusServiceClient, embeddingModel)
                .collectionName(collectionName)
                .databaseName(properties.getDatabaseName())
                .embeddingDimension(properties.getEmbeddingDimension())
                .indexParameters(properties.getIndexParameters())
                .initializeSchema(properties.isInitializeSchema())
                .build();

        vectorStore.afterPropertiesSet();
        return vectorStore;
    }
}