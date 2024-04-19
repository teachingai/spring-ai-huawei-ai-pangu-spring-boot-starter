package org.springframework.ai.huaweiai.pangu.autoconfigure;

import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguChatOptions;
import org.springframework.ai.huaweiai.pangu.util.ApiUtils;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

@ConfigurationProperties(HuaweiAiPanguChatProperties.CONFIG_PREFIX)
public class HuaweiAiPanguChatProperties extends HuaweiAiParentProperties {

    public static final String CONFIG_PREFIX = "spring.ai.huaweiai.pangu.chat";


    /**
     * Enable Huawei Pangu chat client.
     */
    private boolean enabled = true;

    /**
     * Client lever Huawei Pangu options. Use this property to configure generative temperature,
     * topK and topP and alike parameters. The null values are ignored defaulting to the
     * generative's defaults.
     */
    @NestedConfigurationProperty
    private HuaweiAiPanguChatOptions options = HuaweiAiPanguChatOptions.builder()
            .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
            .build();

    public HuaweiAiPanguChatOptions getOptions() {
        return this.options;
    }

    public void setOptions(HuaweiAiPanguChatOptions options) {
        this.options = options;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
