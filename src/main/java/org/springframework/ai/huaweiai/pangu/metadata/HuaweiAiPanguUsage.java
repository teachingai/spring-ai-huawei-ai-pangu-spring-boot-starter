package org.springframework.ai.huaweiai.pangu.metadata;

import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class HuaweiAiPanguUsage implements Usage {

    public static HuaweiAiPanguUsage from(PanguUsage usage) {
        return new HuaweiAiPanguUsage(usage);
    }

    private final PanguUsage usage;

    protected HuaweiAiPanguUsage(PanguUsage usage) {
        Assert.notNull(usage, "Huawei AI PanguUsage must not be null");
        this.usage = usage;
    }

    protected PanguUsage getUsage() {
        return this.usage;
    }

    @Override
    public Long getPromptTokens() {
        return getUsage().getPromptTokens();
    }

    @Override
    public Long getGenerationTokens() {
        return getUsage().getCompletionTokens();
    }

    @Override
    public Long getTotalTokens() {
        return getUsage().getTotalTokens();
    }

    @Override
    public String toString() {
        return getUsage().toString();
    }

}
