package ir.msob.manak.memory.vectordb;

import ir.msob.jima.core.commons.filter.Param;
import ir.msob.manak.domain.model.memory.model.QueryRequest;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.memory.util.FilterUtils;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;

import java.util.List;
import java.util.Map;

public abstract class VectorCrudRepository {

    private final VectorStore vectorStore;

    protected VectorCrudRepository(VectorStoreFactory vectorStoreFactory) {
        this.vectorStore = vectorStoreFactory.buildVectorStore(getCollectionName());
    }

    protected abstract String getCollectionName();

    public void save(String id, String text, Map<String, Object> metadata) {
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

    private @NotNull VectorDocument prepareDocument(Document document) {
        return VectorDocument.builder()
                .id(document.getId())
                .text(document.getText())
                .metadata(document.getMetadata())
                .build();
    }

    public void save(VectorDocument document) {
        save(List.of(document));
    }

    public void save(List<VectorDocument> documents) {
        vectorStore.add(documents.stream()
                .map(this::prepareDocument).toList()
        );
    }

    public void update(String id, String text, Map<String, Object> metadata) {
        VectorDocument document = prepareDocument(id, text, metadata);
        update(document);
    }

    public void update(VectorDocument document) {
        update(List.of(document));
    }

    public void update(List<VectorDocument> documents) {
        delete(documents.stream().map(VectorDocument::getId).toList());
        save(documents);
    }

    public void delete(String... id) {
        delete(List.of(id));
    }

    public void delete(List<String> idList) {
        vectorStore.delete(idList);
    }

    public void delete(Filter.Expression filterExpression) {
        vectorStore.delete(filterExpression);
    }

    public void delete(String filterExpression) {
        vectorStore.delete(filterExpression);
    }

    public List<VectorDocument> query(String query) {
        return vectorStore.similaritySearch(query)
                .stream()
                .map(this::prepareDocument)
                .toList();
    }

    public List<VectorDocument> query(String query, Integer topK, Double similarityThreshold, Map<String, Param<?>> filterParams) {

        Filter.Expression filter = FilterUtils.buildFilterFromParams(filterParams);

        SearchRequest.Builder reqBuilder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(similarityThreshold);

        if (filter != null) {
            reqBuilder.filterExpression(filter);
        }

        SearchRequest req = reqBuilder.build();

        return vectorStore.similaritySearch(req)
                .stream()
                .map(this::prepareDocument)
                .toList();
    }

    public List<VectorDocument> query(QueryRequest query) {
        if (query == null) {
            throw new IllegalArgumentException("query must not be null");
        }
        return query(query.getQuery(), query.getTopK(), query.getSimilarityThreshold(), query.getMetadata());
    }
}
