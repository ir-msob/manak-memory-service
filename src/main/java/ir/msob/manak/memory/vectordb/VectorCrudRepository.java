package ir.msob.manak.memory.vectordb;

import ir.msob.manak.domain.model.memory.model.VectorDocument;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;
import java.util.Map;


public interface VectorCrudRepository {

    VectorStore getVectorStore();

    default void save(String id, String text, Map<String, Object> metadata) {
        VectorDocument document = prepareDocument(id, text, metadata);
        save(document);
    }

    private @NotNull VectorDocument prepareDocument(String id, String text, Map<String, Object> metadata) {
        return VectorDocument.builder()
                .id(id)
                .text(text)
                .metadata(metadata)
                .build();
    }

    private @NotNull Document prepareDocument(VectorDocument vectorDocument) {
        return Document.builder()
                .id(vectorDocument.getId())
                .text(vectorDocument.getText())
                .metadata(vectorDocument.getMetadata())
                .build();
    }

    default void save(VectorDocument document) {
        save(List.of(document));
    }

    default void save(List<VectorDocument> documents) {
        getVectorStore().add(documents.stream()
                .map(this::prepareDocument).toList()
        );
    }

    default void update(String id, String text, Map<String, Object> metadata) {
        VectorDocument document = prepareDocument(id, text, metadata);
        update(document);
    }

    default void update(VectorDocument document) {
        update(List.of(document));
    }

    default void update(List<VectorDocument> documents) {
        delete(documents.stream().map(VectorDocument::getId).toList());
        save(documents);
    }

    default void delete(String... id) {
        delete(List.of(id));
    }

    default void delete(List<String> idList) {
        getVectorStore().delete(idList);
    }

    default void delete(Filter.Expression filterExpression) {
        getVectorStore().delete(filterExpression);
    }

    default void delete(String filterExpression) {
        getVectorStore().delete(filterExpression);
    }

}
