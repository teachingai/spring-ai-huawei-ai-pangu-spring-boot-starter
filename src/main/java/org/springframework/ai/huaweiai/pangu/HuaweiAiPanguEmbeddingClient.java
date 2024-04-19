package org.springframework.ai.huaweiai.pangu;

import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguClient;
import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguUsage;
import com.huaweicloud.pangu.dev.sdk.client.pangu.embedding.PanguEmbedding;
import com.huaweicloud.pangu.dev.sdk.client.pangu.embedding.PanguEmbeddingReq;
import com.huaweicloud.pangu.dev.sdk.client.pangu.embedding.PanguEmbeddingResp;
import com.huaweicloud.pangu.dev.sdk.exception.PanguDevSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.*;
import org.springframework.ai.huaweiai.pangu.util.LlmUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class HuaweiAiPanguEmbeddingClient extends AbstractEmbeddingClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final HuaweiAiPanguEmbeddingOptions defaultOptions;

    private final MetadataMode metadataMode;

    /**
     * Low-level Huawei Pangu API library.
     */
    private final PanguClient panguClient;

    private final RetryTemplate retryTemplate;

    public HuaweiAiPanguEmbeddingClient(PanguClient panguClient) {
        this(panguClient, MetadataMode.EMBED);
    }

    public HuaweiAiPanguEmbeddingClient(PanguClient panguClient, MetadataMode metadataMode) {
        this(panguClient, metadataMode, HuaweiAiPanguEmbeddingOptions.builder().build(), RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiPanguEmbeddingClient(PanguClient panguClient, MetadataMode metadataMode, HuaweiAiPanguEmbeddingOptions options) {
        this(panguClient, metadataMode, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiPanguEmbeddingClient(PanguClient panguClient, MetadataMode metadataMode, HuaweiAiPanguEmbeddingOptions options, RetryTemplate retryTemplate) {
        Assert.notNull(panguClient, "panguClient must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");
        Assert.notNull(retryTemplate, "retryTemplate must not be null");

        this.panguClient = panguClient;
        this.metadataMode = metadataMode;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public List<Double> embed(Document document) {
        Assert.notNull(document, "Document must not be null");
        return this.embed(document.getFormattedContent(this.metadataMode));
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        Assert.notEmpty(request.getInstructions(), "At least one text is required!");
        // execute the request
        return this.retryTemplate.execute(ctx -> {
            logger.debug("Retrieving embeddings");
            // Use tenant specific client if available.
            PanguClient llmClient;
            if(request.getOptions() != null && request.getOptions() instanceof HuaweiAiPanguEmbeddingTenantOptions embeddingOptions){
                llmClient = LlmUtils.getOrCreatePanguClient(embeddingOptions)
                        .orElseThrow(() -> new PanguDevSDKException("PanguClient initialization failed for Tenant Request."));
            } else {
                llmClient = this.panguClient;
            }
            var apiRequest = toEmbeddingRequest(request);
            PanguEmbeddingResp apiEmbeddingResponse = llmClient.createEmbeddings(apiRequest);
            if (Objects.isNull(apiEmbeddingResponse) || CollectionUtils.isEmpty(apiEmbeddingResponse.getData())){
                logger.warn("No embeddings returned for request: {}", request);
                return new EmbeddingResponse(List.of());
            }
            logger.debug("Embeddings retrieved");
            return generateEmbeddingResponse(apiEmbeddingResponse);

        });
    }

    private PanguEmbeddingReq toEmbeddingRequest(EmbeddingRequest request) {
        var panguEmbeddingReq = (this.defaultOptions != null)
                ? PanguEmbeddingReq.builder().input(request.getInstructions()).user(this.defaultOptions.getUser()).build()
                : PanguEmbeddingReq.builder().input(request.getInstructions()).build();

        if (request.getOptions() != null && !EmbeddingOptions.EMPTY.equals(request.getOptions())) {
            panguEmbeddingReq = ModelOptionsUtils.merge(request.getOptions(), panguEmbeddingReq, PanguEmbeddingReq.class);
        }
        return panguEmbeddingReq;
    }

    private EmbeddingResponse generateEmbeddingResponse(PanguEmbeddingResp embeddingResponse) {
        List<Embedding> data = generateEmbeddingList(embeddingResponse.getData());
        EmbeddingResponseMetadata metadata = generateMetadata(embeddingResponse.getUsage());
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

    private EmbeddingResponseMetadata generateMetadata(PanguUsage embeddingsUsage) {
        EmbeddingResponseMetadata metadata = new EmbeddingResponseMetadata();
        // metadata.put("model", model);
        metadata.put("prompt-tokens", embeddingsUsage.getPromptTokens());
        metadata.put("completion-tokens", embeddingsUsage.getCompletionTokens());
        metadata.put("total-tokens", embeddingsUsage.getTotalTokens());
        return metadata;
    }

}
