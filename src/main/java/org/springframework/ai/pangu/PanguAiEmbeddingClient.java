package org.springframework.ai.pangu;

import com.baidubce.qianfan.Qianfan;
import com.baidubce.qianfan.model.embedding.EmbeddingData;
import com.baidubce.qianfan.model.embedding.EmbeddingUsage;
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

public class PanguAiEmbeddingClient extends AbstractEmbeddingClient {

    private final Logger logger = LoggerFactory.getLogger(getClass());

    private final PanguAiEmbeddingOptions defaultOptions;

    private final MetadataMode metadataMode;

    /**
     * Low-level 百度千帆 API library.
     */
    private final Qianfan qianfan;

    public PanguAiEmbeddingClient(Qianfan qianfan) {
        this(qianfan, MetadataMode.EMBED);
    }

    public PanguAiEmbeddingClient(Qianfan qianfan, MetadataMode metadataMode) {
        this(qianfan, metadataMode, PanguAiEmbeddingOptions.builder().build());
    }

    public PanguAiEmbeddingClient(Qianfan qianfan, MetadataMode metadataMode, PanguAiEmbeddingOptions options) {
        Assert.notNull(qianfan, "Qianfan must not be null");
        Assert.notNull(metadataMode, "metadataMode must not be null");
        Assert.notNull(options, "options must not be null");

        this.qianfan = qianfan;
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

        com.baidubce.qianfan.model.embedding.EmbeddingRequest embeddingRequest = this.toEmbeddingRequest(request);
        com.baidubce.qianfan.model.embedding.EmbeddingResponse embeddingResponse = qianfan.embedding(embeddingRequest);
        if (embeddingResponse == null) {
            logger.warn("No embeddings returned for request: {}", request);
            return new EmbeddingResponse(List.of());
        }

        logger.debug("Embeddings retrieved");
        return generateEmbeddingResponse(embeddingRequest.getModel(), embeddingResponse);
    }

    com.baidubce.qianfan.model.embedding.EmbeddingRequest toEmbeddingRequest(EmbeddingRequest embeddingRequest) {
        var qianfanRequest = new com.baidubce.qianfan.model.embedding.EmbeddingRequest();
        qianfanRequest.setInput(embeddingRequest.getInstructions());
        if (this.defaultOptions != null) {
            qianfanRequest.setModel(this.defaultOptions.getModel());
            //qianfanRequest.setUserId(this.defaultOptions.getUser());
            //qianfanRequest.setExtraParameters(this.defaultOptions.getExtraParameters());
        }
        if (embeddingRequest.getOptions() != null && !EmbeddingOptions.EMPTY.equals(embeddingRequest.getOptions())) {
            qianfanRequest = ModelOptionsUtils.merge(embeddingRequest.getOptions(), qianfanRequest,
                    com.baidubce.qianfan.model.embedding.EmbeddingRequest.class);
        }
        return qianfanRequest;
    }

    private EmbeddingResponse generateEmbeddingResponse(String model, com.baidubce.qianfan.model.embedding.EmbeddingResponse embeddingResponse) {
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
