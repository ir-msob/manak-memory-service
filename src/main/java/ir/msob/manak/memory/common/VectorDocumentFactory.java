package ir.msob.manak.memory.common;

import ir.msob.manak.core.service.jima.service.IdService;
import ir.msob.manak.domain.model.memory.model.VectorDocument;
import ir.msob.manak.domain.model.util.chunk.ChunkFile;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
@RequiredArgsConstructor
public class VectorDocumentFactory {


    private final IdService idService;


    public VectorDocument fromChunk(String idKey, String idValue, String filePath, ChunkFile chunk, int totalChunks) {
        Map<String, Object> metadata = new HashMap<>();
        metadata.put(idKey, idValue);
        metadata.put("source", filePath);
        metadata.put("index", chunk.getIndex());
        metadata.put("startLine", chunk.getStartLine());
        metadata.put("endLine", chunk.getEndLine());
        metadata.put("totalChunks", totalChunks);


        return VectorDocument.builder()
                .id(idService.newId())
                .text(chunk.getText())
                .metadata(metadata)
                .build();
    }


    public List<VectorDocument> fromChunks(String idKey, String idValue, String filePath, List<ChunkFile> chunks) {
        int total = chunks.size();
        return chunks.stream().map(c -> fromChunk(idKey, idValue, filePath, c, total)).collect(Collectors.toList());
    }
}