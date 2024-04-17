package org.springframework.ai.huaweiai.pangu;

import com.huaweicloud.pangu.dev.sdk.api.embedings.Embeddings;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMParamConfig;
import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguUsage;
import com.huaweicloud.pangu.dev.sdk.client.pangu.embedding.PanguEmbedding;
import com.huaweicloud.pangu.dev.sdk.client.pangu.embedding.PanguEmbeddingReq;
import com.huaweicloud.pangu.dev.sdk.client.pangu.embedding.PanguEmbeddingResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class HuaweiAiPanguEmbeddingClient extends AbstractEmbeddingClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HuaweiAiPanguEmbeddingOptions defaultOptions;

    private final MetadataMode metadataMode;

    /**
     * Low-level 百度千帆 API library.
     */
    private final com.huaweicloud.pangu.dev.sdk.api.embedings.Embedding embedder;

    private final RetryTemplate retryTemplate;

    public HuaweiAiPanguEmbeddingClient(com.huaweicloud.pangu.dev.sdk.api.embedings.Embedding embedder) {
        this(embedder, MetadataMode.EMBED);
    }

    public HuaweiAiPanguEmbeddingClient(com.huaweicloud.pangu.dev.sdk.api.embedings.Embedding embedder, MetadataMode metadataMode) {
        this(embedder, metadataMode, HuaweiAiPanguEmbeddingOptions.builder().build());
    }

    public HuaweiAiPanguEmbeddingClient(com.huaweicloud.pangu.dev.sdk.api.embedings.Embedding embedder, HuaweiAiPanguEmbeddingOptions options) {
        this(embedder, MetadataMode.EMBED, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiPanguEmbeddingClient(com.huaweicloud.pangu.dev.sdk.api.embedings.Embedding embedder, MetadataMode metadataMode, HuaweiAiPanguEmbeddingOptions options, RetryTemplate retryTemplate) {
        Assert.notNull(embedder, "embedder must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");

        this.embedder = embedder;
        this.metadataMode = metadataMode;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public List<Double> embed(Document document) {
        return this.retryTemplate.execute(ctx -> {
            logger.debug("Retrieving embeddings");
            List<List<Float>> response = embedder.embedDocuments(List.of(document.getFormattedContent(this.metadataMode)));
            logger.debug("Embeddings retrieved");
            return response.stream().flatMap(List::stream).map(Float::doubleValue).toList();
        });
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return this.retryTemplate.execute(ctx -> {

            logger.debug("Retrieving embeddings");

            Assert.notEmpty(request.getInstructions(), "At least one text is required!");
            if (request.getInstructions().size() != 1) {
                logger.warn( "Moonshot AI Embedding does not support batch embedding. Will make multiple API calls to embed(Document)");
            }
            var inputContent = CollectionUtils.firstElement(request.getInstructions());
            var apiRequest = (this.defaultOptions != null)
                    ? new MoonshotAiApi.EmbeddingRequest(inputContent, this.defaultOptions.getModel())
                    : new MoonshotAiApi.EmbeddingRequest(inputContent, MoonshotAiApi.EmbeddingModel.EMBED.getValue());

            if (request.getOptions() != null && !EmbeddingOptions.EMPTY.equals(request.getOptions())) {
                apiRequest = ModelOptionsUtils.merge(request.getOptions(), apiRequest, MoonshotAiApi.EmbeddingRequest.class);
            }
            LLMConfig llmConfig =  LLMConfig.builder().llmParamConfig(LLMParamConfig.builder().temperature(0.9).build()).build();


            List<Float> embedding = embedder.embedQuery(text);
            var apiEmbeddingResponse = this.moonshotAiApi.embeddings(apiRequest).getBody();

            if (CollectionUtils.isEmpty(embedding)) {
                logger.warn("No embeddings returned for request: {}", request);
                return new EmbeddingResponse(List.of());
            }
            logger.debug("Embeddings retrieved");
            var metadata = generateResponseMetadata(apiEmbeddingResponse.model(), apiEmbeddingResponse.usage());

            var embeddings = apiEmbeddingResponse.data()
                    .stream()
                    .map(e -> new Embedding(e.embedding(), e.index()))
                    .toList();


            return new EmbeddingResponse(embeddings, metadata);

        });
    }

    private PanguEmbeddingReq toEmbeddingRequest(EmbeddingRequest embeddingRequest) {
        var panguEmbeddingReq = PanguEmbeddingReq.builder().input(embeddingRequest.getInstructions()).build();
        if (embeddingRequest.getOptions() != null && !EmbeddingOptions.EMPTY.equals(embeddingRequest.getOptions())) {
            panguEmbeddingReq = ModelOptionsUtils.merge(embeddingRequest.getOptions(), panguEmbeddingReq,
                    PanguEmbeddingReq.class);
        }
        return panguEmbeddingReq;
    }

    private EmbeddingResponse generateEmbeddingResponse(String model, PanguEmbeddingResp embeddingResponse) {
        List<Embedding> data = generateEmbeddingList(embeddingResponse.getData());
        EmbeddingResponseMetadata metadata = generateMetadata(model, embeddingResponse.getUsage());
        return new EmbeddingResponse(data, metadata);
    }

    private List<Embedding> generateEmbeddingList(List<PanguEmbedding> nativeData) {
        List<Embedding> data = new ArrayList<>();
        for (PanguEmbedding nativeDatum : nativeData) {
            List<Float> nativeDatumEmbedding = nativeDatum.getEmbedding();
            int nativeIndex = nativeDatum.getIndex();
            Embedding embedding = new Embedding(nativeDatumEmbedding.stream().map(Float::doubleValue).collect(Collectors.toList()), nativeIndex);
            data.add(embedding);
        }
        return data;
    }

    private EmbeddingResponseMetadata generateMetadata(String model, PanguUsage embeddingsUsage) {
        EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
        // metadata.put("model", model);
        metadata.put("prompt-tokens", embeddingsUsage.getPromptTokens());
        metadata.put("total-tokens", embeddingsUsage.getTotalTokens());
        return metadata;
    }


}
