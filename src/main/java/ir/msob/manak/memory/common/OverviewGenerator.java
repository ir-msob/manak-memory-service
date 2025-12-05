package ir.msob.manak.memory.common;

import ir.msob.manak.domain.model.memory.model.VectorDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component
@RequiredArgsConstructor
public class OverviewGenerator {


    private final SummarizerService summarizer;
    private final ir.msob.manak.core.service.jima.service.IdService idService;


    public VectorDocument generate(String idKey, String idValue, String sourcePath, List<VectorDocument> chunks) {
        List<String> texts = chunks.stream().map(VectorDocument::getText).toList();
        String summary = summarizer.summarize(texts);
        return VectorDocument.builder()
                .id(idService.newId())
                .text(summary)
                .metadata(Map.of(
                        idKey, idValue,
                        "source", sourcePath
                ))
                .build();
    }
}