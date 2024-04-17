package org.springframework.ai.huaweiai.pangu.autoconfigure;

import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.api.memory.cache.Caches;
import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguChatClient;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguEmbeddingClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for 百度千帆 Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ HuaweiAiPanguChatProperties.class, HuaweiAiPanguConnectionProperties.class, HuaweiAiPanguEmbeddingProperties.class })
@ConditionalOnClass(LLMs.class)
public class HuaweiAiPanguAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLM llm(HuaweiAiPanguConnectionProperties properties) {
        //Assert.isNull(properties.getType(), "llm Type must be set");
        Assert.hasText(properties.getAccessKey(), "llm API Access Key must be set");
        Assert.hasText(properties.getSecretKey(), "llm API Secret Key must be set");

        LLMConfig llmConfig = new LLMConfig();
        LLM llm = LLMs.of(LLMs.PANGU, llmConfig);
        llm.setCache(Caches.of(Caches.IN_MEMORY));

        return llm;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiPanguChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguChatClient panguAiChatClient(LLM llm,
                                                     HuaweiAiPanguChatProperties chatProperties,
                                                     List<FunctionCallback> toolFunctionCallbacks,
                                                     FunctionCallbackContext functionCallbackContext) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        return new HuaweiAiPanguChatClient(llm, chatProperties.getOptions(), functionCallbackContext);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguEmbeddingClient panguAiEmbeddingClient(LLM llm, HuaweiAiPanguEmbeddingProperties embeddingProperties) {

        return new HuaweiAiPanguEmbeddingClient(llm, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions());
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
