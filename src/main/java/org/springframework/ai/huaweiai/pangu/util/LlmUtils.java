package org.springframework.ai.huaweiai.pangu.util;

import com.huaweicloud.pangu.dev.sdk.api.embedings.config.EmbeddingConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguClient;
import com.huaweicloud.pangu.dev.sdk.llms.module.Pangu;
import com.huaweicloud.pangu.dev.sdk.utils.SecurityUtil;
import org.springframework.ai.huaweiai.pangu.*;
import org.springframework.util.Assert;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.StringJoiner;
import java.util.concurrent.ConcurrentHashMap;

public class LlmUtils {

    private static Map<String, Optional<Pangu>> LLM_MAP = new ConcurrentHashMap<>();
    private static Map<String, Optional<PanguClient>> LLM_CHAT_CLIENT_MAP = new ConcurrentHashMap<>();
    private static Map<String, Optional<PanguClient>> LLM_EMBEDDING_CLIENT_MAP = new ConcurrentHashMap<>();

    public static Pangu createLlm(LLMConfig llmConfig){
        return (Pangu) LLMs.of(LLMs.PANGU, llmConfig);
    }

    /**
     * 获取联合键
     * @param chatOptions
     * @return
     */
    public static String getUnionKey(HuaweiAiPanguChatOptions chatOptions) {
        if(chatOptions instanceof HuaweiAiPanguChatTenantOptions tenantOptions){
            StringJoiner joiner = new StringJoiner(".");
            HuaweiAiPanguIamOptions iamOptions = tenantOptions.getIamOptions();
            if(Objects.nonNull(iamOptions)){
                joiner.add(iamOptions.getUnionKey());
            }
            HuaweiAiPanguModuleOptions moduleOptions = tenantOptions.getModuleOptions();
            if(Objects.nonNull(moduleOptions)){
                joiner.add(moduleOptions.getUnionKey());
            }
            HuaweiAiPanguHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
            if(Objects.nonNull(httpProxyOptions)){
                joiner.add(httpProxyOptions.getUnionKey());
            }
            return joiner.toString();
        }
        return SecurityUtil.getUnionKey(LLMs.PANGU);
    }

    /**
     * 获取联合键
     * @param embeddingOptions
     * @return
     */
    public static String getUnionKey(HuaweiAiPanguEmbeddingOptions embeddingOptions) {
        if(embeddingOptions instanceof HuaweiAiPanguEmbeddingTenantOptions tenantOptions){
            StringJoiner joiner = new StringJoiner(".");
            HuaweiAiPanguIamOptions iamOptions = tenantOptions.getIamOptions();
            if(Objects.nonNull(iamOptions)){
                joiner.add(iamOptions.getUnionKey());
            }
            HuaweiAiPanguHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
            if(Objects.nonNull(httpProxyOptions)){
                joiner.add(httpProxyOptions.getUnionKey());
            }
            return joiner.toString();
        }
        return SecurityUtil.getUnionKey(LLMs.PANGU, embeddingOptions.getModel());
    }

    /**
     * 获取或创建 Pangu
     * @param chatOptions 聊天配置
     * @return Pangu
     */
    public static Optional<Pangu> getOrCreatePanguLLM(HuaweiAiPanguChatOptions chatOptions) {
        return LLM_MAP.computeIfAbsent(LlmUtils.getUnionKey(chatOptions), unionKey -> {
            // 判断是否为独立租户聊天配置
            if(chatOptions instanceof HuaweiAiPanguChatTenantOptions tenantOptions){
                // 如果没有实时指定IAM配置、模型配置，则认为无需区分 LLM 客户端
                HuaweiAiPanguIamOptions iamOptions = tenantOptions.getIamOptions();
                HuaweiAiPanguModuleOptions moduleOptions = tenantOptions.getModuleOptions();
                if(Objects.isNull(iamOptions) && Objects.isNull(moduleOptions) ){
                    return Optional.empty();
                }
                // 构建LLMConfig
                LLMConfig llmConfig = LLMConfig.builder()
                        .iamConfig(ApiUtils.toIAMConfig(iamOptions))
                        .llmParamConfig(ApiUtils.toLLMParamConfig(tenantOptions))
                        .llmModuleConfig(ApiUtils.toLLMModuleConfig(moduleOptions))
                        .build();
                HuaweiAiPanguHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
                if(Objects.nonNull(httpProxyOptions)){
                    llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(httpProxyOptions));
                }
                // 构建LLM
                LLM pangu = LLMs.of(LLMs.PANGU, llmConfig);
                // TODO 增加缓存配置
                //llm.setCache(Caches.of(Caches.IN_MEMORY));
                return Optional.ofNullable((Pangu) pangu);
            }
            return Optional.empty();
        });
    }

    /**
     * 获取或创建 PanguClient
     * @param chatOptions 聊天配置
     * @return PanguClient
     */
    public static Optional<PanguClient> getOrCreatePanguClient(HuaweiAiPanguChatOptions chatOptions) {
        return LLM_CHAT_CLIENT_MAP.computeIfAbsent(LlmUtils.getUnionKey(chatOptions), unionKey -> {
            // 判断是否为独立租户聊天配置
            if(chatOptions instanceof HuaweiAiPanguChatTenantOptions tenantOptions){
                // 如果没有实时指定IAM配置、模型配置，则认为无需区分 LLM 客户端
                HuaweiAiPanguIamOptions iamOptions = tenantOptions.getIamOptions();
                HuaweiAiPanguModuleOptions moduleOptions = tenantOptions.getModuleOptions();
                if(Objects.isNull(iamOptions) && Objects.isNull(moduleOptions) ){
                    return Optional.empty();
                }
                // 构建LLMConfig
                LLMConfig llmConfig = LLMConfig.builder()
                        .iamConfig(ApiUtils.toIAMConfig(iamOptions))
                        .llmParamConfig(ApiUtils.toLLMParamConfig(tenantOptions))
                        .llmModuleConfig(ApiUtils.toLLMModuleConfig(moduleOptions))
                        .build();
                HuaweiAiPanguHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
                if(Objects.nonNull(httpProxyOptions)){
                    llmConfig.setHttpConfig(ApiUtils.toHTTPConfig(httpProxyOptions));
                }

                EmbeddingConfig embeddingConfig = ApiUtils.toEmbeddingConfig(tenantOptions);
                return Optional.ofNullable(new PanguClient(llmConfig, embeddingConfig));
            }
            return Optional.empty();
        });
    }

    /**
     * 获取或创建 PanguClient
     * @param embeddingOptions 嵌入配置
     * @return PanguClient
     */
    public static Optional<PanguClient> getOrCreatePanguClient(HuaweiAiPanguEmbeddingOptions embeddingOptions) {
        return LLM_EMBEDDING_CLIENT_MAP.computeIfAbsent(LlmUtils.getUnionKey(embeddingOptions), key -> {
            // 判断是否为独立租户 embedding 配置
            if(embeddingOptions instanceof HuaweiAiPanguEmbeddingTenantOptions tenantOptions){
                // 如果没有实时指定IAM配置、模型配置，则认为无需区分 LLM 客户端
                HuaweiAiPanguIamOptions iamOptions = tenantOptions.getIamOptions();
                if(Objects.isNull(iamOptions)){
                    return Optional.empty();
                }
                Assert.hasText(tenantOptions.getBaseUrl(), "Huawei AI Pangu base URL must be set");

                EmbeddingConfig embeddingConfig = EmbeddingConfig.builder()
                        .url(tenantOptions.getBaseUrl())
                        .embeddingName(tenantOptions.getModel())
                        .iamConfig(ApiUtils.toIAMConfig(iamOptions))
                        .build();
                HuaweiAiPanguHttpProxyOptions httpProxyOptions = tenantOptions.getHttpProxyOptions();
                if(Objects.nonNull(httpProxyOptions)){
                    embeddingConfig.setHttpConfig(ApiUtils.toHTTPConfig(httpProxyOptions));
                }
                return Optional.ofNullable(new PanguClient(embeddingConfig));
            }
            return Optional.empty();
        });
    }

}
