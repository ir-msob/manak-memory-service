package ir.msob.manak.memory.common;

import ir.msob.manak.core.service.jima.service.IdService;
import ir.msob.manak.domain.model.ai.summarizer.SummarizerRequestDto;
import ir.msob.manak.domain.model.ai.summarizer.SummarizerResponseDto;
import ir.msob.manak.domain.service.client.AiClient;
import ir.msob.manak.domain.service.properties.ManakProperties;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class SummarizerService {

    private final AiClient aiClient;
    private final IdService idService;
    private final ManakProperties manakProperties;

    @SneakyThrows
    public String summarize(List<String> texts) {

        SummarizerRequestDto summarizerRequestDto = SummarizerRequestDto.builder()
                .requestId(idService.newId())
                .model(manakProperties.getMemory().getAbstractiveSummary().getModel())
                .inputs(texts)
                .type(SummarizerRequestDto.SummaryType.COMBINED)
                .style(SummarizerRequestDto.SummaryStyle.DEFAULT)
                .build();

        SummarizerResponseDto summarizerResponseDto = aiClient.summarize(summarizerRequestDto)
                .toFuture()
                .get();

        return summarizerResponseDto.getFinalSummary();
    }

}
