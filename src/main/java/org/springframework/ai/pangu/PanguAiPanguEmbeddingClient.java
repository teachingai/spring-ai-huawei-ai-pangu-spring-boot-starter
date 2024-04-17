package org.springframework.ai.pangu;

import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMParamConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PanguAiPanguEmbeddingClient extends AbstractEmbeddingClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PanguAiPanguEmbeddingOptions defaultOptions;

    private final MetadataMode metadataMode;

    /**
     * Low-level 百度千帆 API library.
     */
    private final LLM llm;

    public PanguAiPanguEmbeddingClient(LLM llm) {
        this(llm, MetadataMode.EMBED);
    }

    public PanguAiPanguEmbeddingClient(LLM llm, MetadataMode metadataMode) {
        this(llm, metadataMode, PanguAiPanguEmbeddingOptions.builder().build());
    }

    public PanguAiPanguEmbeddingClient(LLM llm, MetadataMode metadataMode, PanguAiPanguEmbeddingOptions options) {
        Assert.notNull(llm, "llm must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");

        this.llm = llm;
        this.metadataMode = metadataMode;
        this.defaultOptions = options;
    }

    @Override
    public List<Double> embed(Document document) {
        logger.debug("Retrieving embeddings");
        EmbeddingResponse response = this.call(new EmbeddingRequest(List.of(document.getFormattedContent(this.metadataMode)), null));
        logger.debug("Embeddings retrieved");
        return response.getResults().stream().map(embedding -> embedding.getOutput()).flatMap(List::stream).toList();
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {

        logger.debug("Retrieving embeddings");

        LLMConfig llmConfig =  LLMConfig.builder().llmParamConfig(LLMParamConfig.builder().temperature(0.9).build()).build();


        com.baidubce.llm.model.embedding.EmbeddingRequest embeddingRequest = this.toEmbeddingRequest(request);
        com.baidubce.llm.model.embedding.EmbeddingResponse embeddingResponse = llm.embedding(embeddingRequest);
        if (embeddingResponse == null) {
            logger.warn("No embeddings returned for request: {}", request);
            return new EmbeddingResponse(List.of());
        }

        logger.debug("Embeddings retrieved");
        return generateEmbeddingResponse(embeddingRequest.getModel(), embeddingResponse);
    }

    com.baidubce.llm.model.embedding.EmbeddingRequest toEmbeddingRequest(EmbeddingRequest embeddingRequest) {
        var llmRequest = new com.baidubce.llm.model.embedding.EmbeddingRequest();
        llmRequest.setInput(embeddingRequest.getInstructions());
        if (this.defaultOptions != null) {
            llmRequest.setModel(this.defaultOptions.getModel());
            //llmRequest.setUserId(this.defaultOptions.getUser());
            //llmRequest.setExtraParameters(this.defaultOptions.getExtraParameters());
        }
        if (embeddingRequest.getOptions() != null && !EmbeddingOptions.EMPTY.equals(embeddingRequest.getOptions())) {
            llmRequest = ModelOptionsUtils.merge(embeddingRequest.getOptions(), llmRequest,
                    com.baidubce.llm.model.embedding.EmbeddingRequest.class);
        }
        return llmRequest;
    }

    private EmbeddingResponse generateEmbeddingResponse(String model, com.baidubce.llm.model.embedding.EmbeddingResponse embeddingResponse) {
        List<Embedding> data = generateEmbeddingList(embeddingResponse.getData());
        EmbeddingResponseMetadata metadata = generateMetadata(model, embeddingResponse.getUsage());
        return new EmbeddingResponse(data, metadata);
    }

    private List<Embedding> generateEmbeddingList(List<EmbeddingData> nativeData) {
        List<Embedding> data = new ArrayList<>();
        for (EmbeddingData nativeDatum : nativeData) {
            List<BigDecimal> nativeDatumEmbedding = nativeDatum.getEmbedding();
            int nativeIndex = nativeDatum.getIndex();
            Embedding embedding = new Embedding(nativeDatumEmbedding.stream().map(BigDecimal::doubleValue).collect(Collectors.toList()), nativeIndex);
            data.add(embedding);
        }
        return data;
    }

    private EmbeddingResponseMetadata generateMetadata(String model, EmbeddingUsage embeddingsUsage) {
        EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
        // metadata.put("model", model);
        metadata.put("prompt-tokens", embeddingsUsage.getPromptTokens());
        metadata.put("total-tokens", embeddingsUsage.getTotalTokens());
        return metadata;
    }

}
