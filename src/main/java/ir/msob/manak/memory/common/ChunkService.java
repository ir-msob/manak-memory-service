package ir.msob.manak.memory.common;

import ir.msob.manak.domain.model.util.chunk.ChunkFile;
import ir.msob.manak.domain.model.util.chunk.Chunker;
import ir.msob.manak.domain.service.properties.ManakProperties;
import ir.msob.manak.memory.util.FileTypeDetector;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@RequiredArgsConstructor
public class ChunkService {


    private final ManakProperties manakProperties;


    public List<ChunkFile> chunkBytes(byte[] bytes, String path) {
        Chunker.FileType ft = FileTypeDetector.detect(path);
        return Chunker.chunk(bytes, ft, manakProperties.getMemory().getChunk().getChunkSize(), manakProperties.getMemory().getChunk().getOverlap());
    }


    public List<ChunkFile> chunkInputStream(InputStreamResource isr, Chunker.FileType defaultType) {
        return Chunker.chunk(isr, defaultType, manakProperties.getMemory().getChunk().getChunkSize(), manakProperties.getMemory().getChunk().getOverlap());
    }
}