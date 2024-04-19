package org.springframework.ai.huaweiai.pangu.autoconfigure;

import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguEmbeddingOptions;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HuaweiAiPanguEmbeddingProperties.CONFIG_PREFIX)
public class HuaweiAiPanguEmbeddingProperties extends HuaweiAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.huaweiai.pangu.embedding";

    public static final String DEFAULT_EMBEDDING_MODEL = "embedding-v1";

    /**
     * Enable Huawei Pangu embedding client.
     */
    private boolean enabled = true;

    public MetadataMode metadataMode = MetadataMode.EMBED;

    /**
     * Client lever Huawei Pangu options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private HuaweiAiPanguEmbeddingOptions options = HuaweiAiPanguEmbeddingOptions.builder()
            .withModel(DEFAULT_EMBEDDING_MODEL)
            .build();

    public HuaweiAiPanguEmbeddingOptions getOptions() {
        return this.options;
    }

    public void setOptions(HuaweiAiPanguEmbeddingOptions options) {
        this.options = options;
    }

    public MetadataMode getMetadataMode() {
        return this.metadataMode;
    }

    public void setMetadataMode(MetadataMode metadataMode) {
        this.metadataMode = metadataMode;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
