package ir.msob.manak.memory.embedding;

import ir.msob.jima.core.commons.logger.Logger;
import ir.msob.jima.core.commons.logger.LoggerFactory;
import ir.msob.manak.domain.model.ai.embedding.EmbeddingRequestDto;
import ir.msob.manak.domain.model.ai.embedding.EmbeddingResponseDto;
import ir.msob.manak.domain.service.client.AiClient;
import ir.msob.manak.domain.service.properties.ManakProperties;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.Embedding;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class RestEmbeddingModel implements EmbeddingModel {

    private static final Logger log = LoggerFactory.getLogger(RestEmbeddingModel.class);

    private final AiClient aiClient;
    private final ManakProperties manakProperties;

    /**
     * -----------------------------------------------------------------------
     * Main Embedding Handler
     * -----------------------------------------------------------------------
     */
    @Override
    public @NotNull EmbeddingResponse call(@NotNull EmbeddingRequest request) {
        try {
            EmbeddingRequestDto dto = buildRequestDto(request);
            EmbeddingResponseDto responseDto = sendRequest(dto);
            return convertResponse(responseDto);

        } catch (Exception e) {
            log.error("❌ Embedding call failed", e);
            throw new RuntimeException("Embedding service error", e);
        }
    }

    /**
     * -----------------------------------------------------------------------
     * Build Request DTO
     * -----------------------------------------------------------------------
     */
    private EmbeddingRequestDto buildRequestDto(EmbeddingRequest request) {
        return EmbeddingRequestDto.builder()
                .model(manakProperties.getMemory().getEmbedding().getModel())
                .inputs(request.getInstructions())
                .options(manakProperties.getMemory().getEmbedding().getOptions())
                .build();
    }

    /**
     * -----------------------------------------------------------------------
     * Call AI client
     * -----------------------------------------------------------------------
     */
    private EmbeddingResponseDto sendRequest(EmbeddingRequestDto dto)
            throws ExecutionException, InterruptedException {

        return aiClient.embedding(dto)
                .toFuture()
                .get();
    }

    /**
     * -----------------------------------------------------------------------
     * Convert ResponseDto → Spring EmbeddingResponse
     * -----------------------------------------------------------------------
     */
    private EmbeddingResponse convertResponse(EmbeddingResponseDto dto) {
        List<Embedding> embeddings = dto.getEmbeddings().stream()
                .map(e -> new Embedding(e.getEmbedding(), e.getIndex()))
                .collect(Collectors.toList());

        return new EmbeddingResponse(embeddings);
    }

    /**
     * -----------------------------------------------------------------------
     * Convenience method
     * -----------------------------------------------------------------------
     */
    @Override
    public float @NotNull [] embed(Document document) {
        Assert.notNull(document.getText(), "Text must not be null");
        return embed(document.getText());
    }
}
