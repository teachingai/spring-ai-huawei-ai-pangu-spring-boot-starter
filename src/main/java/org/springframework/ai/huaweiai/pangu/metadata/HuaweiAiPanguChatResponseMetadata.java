package org.springframework.ai.huaweiai.pangu.metadata;

import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatResp;
import org.springframework.ai.chat.metadata.ChatResponseMetadata;
import org.springframework.ai.chat.metadata.EmptyUsage;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.util.Assert;

public class HuaweiAiPanguChatResponseMetadata implements ChatResponseMetadata {

    public static HuaweiAiPanguChatResponseMetadata from(PanguChatResp response) {
        Assert.notNull(response, "Huawei PanguChatResp must not be null");
        HuaweiAiPanguUsage usage = HuaweiAiPanguUsage.from(response.getUsage());
        HuaweiAiPanguChatResponseMetadata chatResponseMetadata = new HuaweiAiPanguChatResponseMetadata(response.getId(), usage);
        return chatResponseMetadata;
    }

    private final String id;
    private final Usage usage;

    public HuaweiAiPanguChatResponseMetadata(String id, HuaweiAiPanguUsage usage) {
        this.id = id;
        this.usage = usage;
    }

    public String getId() {
        return this.id;
    }

    @Override
    public Usage getUsage() {
        Usage usage = this.usage;
        return usage != null ? usage : new EmptyUsage();
    }

}
