package org.springframework.ai.huaweiai.pangu.autoconfigure;

import com.huaweicloud.pangu.dev.sdk.api.callback.StreamCallBack;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguClient;
import com.huaweicloud.pangu.dev.sdk.llms.module.Pangu;
import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguCachedChatClient;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguChatClient;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguEmbeddingClient;
import org.springframework.ai.huaweiai.pangu.util.ApiUtils;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.CollectionUtils;

import java.util.List;

/**
 * {@link AutoConfiguration Auto-configuration} for Huawei Pangu Chat Client.
 */
@AutoConfiguration(after = { RestClientAutoConfiguration.class, SpringAiRetryAutoConfiguration.class })
@EnableConfigurationProperties({ HuaweiAiPanguChatProperties.class, HuaweiAiPanguConnectionProperties.class, HuaweiAiPanguEmbeddingProperties.class,
        HuaweiAiPanguIamProperties.class })
@ConditionalOnClass(LLMs.class)
public class HuaweiAiPanguAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public LLMConfig llmConfig(HuaweiAiPanguConnectionProperties connectionProperties,
                       HuaweiAiPanguChatProperties chatProperties,
                       HuaweiAiPanguIamProperties iamProperties) {
        LLMConfig llmConfig = new LLMConfig();
        llmConfig.setLlmModuleConfig(ApiUtils.toLLMModuleConfig(connectionProperties));
        llmConfig.setLlmParamConfig(ApiUtils.toLLMParamConfig(chatProperties.getOptions());
        llmConfig.setIamConfig(ApiUtils.toIAMConfig(iamProperties));
        llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(connectionProperties.getHttpProxy()));
        return llmConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public StreamCallBack streamCallBack() {
        return ApiUtils.DEFAULT_STREAM_CALLBACK;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiPanguChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguChatClient panguAiChatClient(LLMConfig llmConfig,
                                                     HuaweiAiPanguChatProperties chatProperties,
                                                     List<FunctionCallback> toolFunctionCallbacks,
                                                     ObjectProvider<StreamCallBack> streamCallBackProvider,
                                                     ObjectProvider<RetryTemplate> retryTemplateProvider) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }
        PanguClient panguClient = new PanguClient(llmConfig);
        StreamCallBack streamCallBack = streamCallBackProvider.getIfAvailable(() -> ApiUtils.DEFAULT_STREAM_CALLBACK)
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiPanguChatClient(panguClient, streamCallBack, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiPanguChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguCachedChatClient panguAiCachedChatClient(LLMConfig llmConfig,
                                                     HuaweiAiPanguChatProperties chatProperties,
                                                     List<FunctionCallback> toolFunctionCallbacks,
                                                     ObjectProvider<StreamCallBack> streamCallBackProvider,
                                                     ObjectProvider<RetryTemplate> retryTemplateProvider) {
        if (!CollectionUtils.isEmpty(toolFunctionCallbacks)) {
            chatProperties.getOptions().getFunctionCallbacks().addAll(toolFunctionCallbacks);
        }

        LLM llm = LLMs.of(LLMs.PANGU, llmConfig);
        // TODO 增加缓存配置
        //llm.setCache(Caches.of(Caches.IN_MEMORY));
        StreamCallBack streamCallBack = streamCallBackProvider.getIfAvailable(() -> ApiUtils.DEFAULT_STREAM_CALLBACK)
        llm.setStreamCallback(streamCallBack);
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiPanguCachedChatClient((Pangu) llm, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguEmbeddingClient panguAiEmbeddingClient(LLMConfig llmConfig,
                                                               HuaweiAiPanguEmbeddingProperties embeddingProperties,
                                                               ObjectProvider<RetryTemplate> retryTemplateProvider) {
        PanguClient panguClient = new PanguClient(llmConfig);
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiPanguEmbeddingClient(panguClient, embeddingProperties.getMetadataMode(), embeddingProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    public FunctionCallbackContext springAiFunctionManager(ApplicationContext context) {
        FunctionCallbackContext manager = new FunctionCallbackContext();
        manager.setApplicationContext(context);
        return manager;
    }

}
