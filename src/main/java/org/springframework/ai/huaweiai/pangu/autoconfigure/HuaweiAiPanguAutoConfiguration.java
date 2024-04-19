package org.springframework.ai.huaweiai.pangu.autoconfigure;

import com.huaweicloud.pangu.dev.sdk.api.embedings.Embeddings;
import com.huaweicloud.pangu.dev.sdk.api.embedings.config.EmbeddingConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguClient;
import org.springframework.ai.autoconfigure.mistralai.MistralAiEmbeddingProperties;
import org.springframework.ai.autoconfigure.retry.SpringAiRetryAutoConfiguration;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguCachedChatClient;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguChatClient;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguEmbeddingClient;
import org.springframework.ai.huaweiai.pangu.util.ApiUtils;
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
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.Objects;

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
        LLMConfig llmConfig = LLMConfig.builder()
                .iamConfig(ApiUtils.toIAMConfig(iamProperties))
                .build();
        if(Objects.nonNull(connectionProperties.getHttpProxy())){
            llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(connectionProperties.getHttpProxy()));
        }
        if(Objects.nonNull(chatProperties.getOptions())){
            llmConfig.setLlmParamConfig(ApiUtils.toLLMParamConfig(chatProperties.getOptions()));
            llmConfig.setLlmModuleConfig(ApiUtils.toLLMModuleConfig(chatProperties, connectionProperties));
        }
        return llmConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingConfig embeddingConfig(HuaweiAiPanguConnectionProperties connectionProperties,
                                           HuaweiAiPanguEmbeddingProperties embeddingProperties,
                                           HuaweiAiPanguIamProperties iamProperties) {

        String baseUrl = StringUtils.hasText(embeddingProperties.getBaseUrl()) ? embeddingProperties.getBaseUrl() : connectionProperties.getBaseUrl();
        Assert.hasText(baseUrl, "Huawei AI Pangu base URL must be set");

        EmbeddingConfig embeddingConfig = EmbeddingConfig.builder()
                .url(baseUrl)
                .embeddingName(Objects.nonNull(embeddingProperties.getOptions()) ? embeddingProperties.getOptions().getModel() : Embeddings.PANGU)
                .iamConfig(ApiUtils.toIAMConfig(iamProperties))
                .build();
        if(Objects.nonNull(connectionProperties.getHttpProxy())){
            embeddingConfig.setHttpConfig(ApiUtils.toHTTPConfig(connectionProperties.getHttpProxy()));
        }
        return embeddingConfig;
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiPanguChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguChatClient panguAiChatClient(LLMConfig llmConfig,
                                                     HuaweiAiPanguChatProperties chatProperties,
                                                     ObjectProvider<RetryTemplate> retryTemplateProvider) {
        PanguClient panguClient = new PanguClient(llmConfig);
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiPanguChatClient(panguClient, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = HuaweiAiPanguChatProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguCachedChatClient panguAiCachedChatClient(LLMConfig llmConfig,
                                                                 HuaweiAiPanguChatProperties chatProperties,
                                                                 ObjectProvider<RetryTemplate> retryTemplateProvider) {
        RetryTemplate retryTemplate = retryTemplateProvider.getIfAvailable(() -> RetryTemplate.builder().build());
        return new HuaweiAiPanguCachedChatClient(llmConfig, chatProperties.getOptions(), retryTemplate);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = MistralAiEmbeddingProperties.CONFIG_PREFIX, name = "enabled", havingValue = "true", matchIfMissing = true)
    public HuaweiAiPanguEmbeddingClient panguAiEmbeddingClient(EmbeddingConfig embeddingConfig,
                                                               HuaweiAiPanguEmbeddingProperties embeddingProperties,
                                                               ObjectProvider<RetryTemplate> retryTemplateProvider) {
        PanguClient panguClient = new PanguClient(embeddingConfig);
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
