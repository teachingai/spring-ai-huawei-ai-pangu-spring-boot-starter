package org.springframework.ai.huaweiai.pangu.util;

import com.huaweicloud.pangu.dev.sdk.api.config.HTTPConfig;
import com.huaweicloud.pangu.dev.sdk.api.config.IAMConfig;
import com.huaweicloud.pangu.dev.sdk.api.embedings.config.EmbeddingConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMModuleConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMParamConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.ConversationMessage;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.Role;
import com.huaweicloud.pangu.dev.sdk.api.llms.response.LLMResp;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatChoice;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatMessage;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatResp;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.huaweiai.pangu.*;
import org.springframework.ai.huaweiai.pangu.autoconfigure.HuaweiAiPanguChatProperties;
import org.springframework.ai.huaweiai.pangu.autoconfigure.HuaweiAiPanguConnectionProperties;
import org.springframework.ai.huaweiai.pangu.autoconfigure.HuaweiAiPanguHttpProxyProperties;
import org.springframework.ai.huaweiai.pangu.autoconfigure.HuaweiAiPanguIamProperties;
import org.springframework.ai.huaweiai.pangu.metadata.HuaweiAiPanguChatResponseMetadata;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.util.*;

public class ApiUtils {

    public static final Float DEFAULT_TEMPERATURE = 0.95f;
    public static final Float DEFAULT_TOP_P = 1.0f;

    public static boolean isTenantRequest(Prompt prompt) {
        if (prompt.getOptions() instanceof HuaweiAiPanguChatTenantOptions chatOptions) {
            return Objects.nonNull(chatOptions.getIamOptions()) || Objects.nonNull(chatOptions.getHttpProxyOptions()) || Objects.nonNull(chatOptions.getModuleOptions());
        }
        if (prompt.getOptions() instanceof HuaweiAiPanguEmbeddingTenantOptions embeddingOptions) {
            return Objects.nonNull(embeddingOptions.getIamOptions()) || Objects.nonNull(embeddingOptions.getHttpProxyOptions()) || Objects.nonNull(embeddingOptions.getModuleOptions());
        }
        return Boolean.FALSE;
    }

    public static LLMModuleConfig toLLMModuleConfig(HuaweiAiPanguModuleOptions moduleOptions){
        if (Objects.isNull(moduleOptions)) {
            return null;
        }
        LLMModuleConfig httpConfig = LLMModuleConfig.builder()
                .LLMName(LLMs.PANGU)
                .url(moduleOptions.getUrl())
                .moduleVersion(moduleOptions.getModuleVersion())
                .enableAppendSystemMessage(moduleOptions.isEnableAppendSystemMessage())
                .systemPrompt(moduleOptions.getSystemPrompt())
                .build();
        return httpConfig;
    }

    public static LLMModuleConfig toLLMModuleConfig(HuaweiAiPanguChatProperties chatProperties,  HuaweiAiPanguConnectionProperties connectionProperties){
        if (Objects.isNull(connectionProperties)) {
            return null;
        }
        String baseUrl = StringUtils.hasText(chatProperties.getBaseUrl()) ? chatProperties.getBaseUrl() : connectionProperties.getBaseUrl();
        Assert.hasText(baseUrl, "Huawei AI Pangu base URL must be set");

        LLMModuleConfig httpConfig = LLMModuleConfig.builder()
                .LLMName(LLMs.PANGU)
                .url(baseUrl)
                .moduleVersion(connectionProperties.getModuleVersion())
                .enableAppendSystemMessage(connectionProperties.isEnableAppendSystemMessage())
                .systemPrompt(connectionProperties.getSystemPrompt())
                .build();
        return httpConfig;
    }

    public static LLMParamConfig toLLMParamConfig(HuaweiAiPanguChatOptions options){
        if (Objects.isNull(options)) {
            return null;
        }
        LLMParamConfig llmParamConfig = LLMParamConfig.builder()
                .maxTokens(options.getMaxTokens())
                .temperature(Objects.nonNull(options.getTemperature()) ?  options.getTemperature().doubleValue() : null)
                .topP(Objects.nonNull(options.getTopK()) ?  options.getTopK().doubleValue() : null)
                .presencePenalty(options.getPresencePenalty())
                .frequencyPenalty(options.getFrequencyPenalty())
                .bestOf(options.getBestOf())
                .withPrompt(options.getWithPrompt())
                .stream(Objects.nonNull(options.getStream()) ? options.getStream() : Boolean.FALSE)
                .build();
        return llmParamConfig;
    }

    public static IAMConfig toIAMConfig(HuaweiAiPanguIamProperties iamProperties){
        if (Objects.isNull(iamProperties)) {
            return null;
        }
        return IAMConfig.builder()
                .iamUrl(iamProperties.getUrl())
                .iamDomain(iamProperties.getDomain())
                .iamUser(iamProperties.getUser())
                .iamPwd(iamProperties.getPassword())
                .projectName(iamProperties.getProjectName())
                .disabled(iamProperties.getDisabled())
                .ak(iamProperties.getAk())
                .sk(iamProperties.getSk())
                .httpConfig(ApiUtils.toHTTPConfig(iamProperties.getHttpProxy()))
                .build();
    }

    public static IAMConfig toIAMConfig(HuaweiAiPanguIamOptions iamOptions){
        if (Objects.isNull(iamOptions)) {
            return null;
        }
        return IAMConfig.builder()
                .iamUrl(iamOptions.getUrl())
                .iamDomain(iamOptions.getDomain())
                .iamUser(iamOptions.getUser())
                .iamPwd(iamOptions.getPassword())
                .projectName(iamOptions.getProjectName())
                .ak(iamOptions.getAk())
                .sk(iamOptions.getSk())
                .build();
    }

    public static HTTPConfig toHTTPConfig(HuaweiAiPanguHttpProxyOptions httpProxyOptions){
        if (Objects.isNull(httpProxyOptions)) {
            return null;
        }
        HTTPConfig httpConfig = HTTPConfig.builder()
                .asyncHttpWaitSeconds(httpProxyOptions.getAsyncHttpWaitSeconds())
                .proxyEnabled(httpProxyOptions.getProxyEnabled())
                .proxyUrl(httpProxyOptions.getProxyUrl())
                .proxyUser(httpProxyOptions.getProxyUser())
                .proxyPassword(httpProxyOptions.getProxyPassword())
                .build();
        return httpConfig;
    }

    public static HTTPConfig toHTTPConfig(HuaweiAiPanguHttpProxyProperties proxyProperties){
        if (Objects.isNull(proxyProperties)) {
            return null;
        }
        HTTPConfig httpConfig = HTTPConfig.builder()
                .asyncHttpWaitSeconds(proxyProperties.getAsyncHttpWaitSeconds())
                .proxyEnabled(proxyProperties.getProxyEnabled())
                .proxyUrl(proxyProperties.getProxyUrl())
                .proxyUser(proxyProperties.getProxyUser())
                .proxyPassword(proxyProperties.getProxyPassword())
                .build();
        return httpConfig;
    }

    public static List<ConversationMessage> toConversationMessage(List<Message> messages){
        if(Objects.isNull(messages)){
            return Collections.emptyList();
        }
        // Build ConversationMessage list from the prompt.
        return messages.stream()
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT
                        || message.getMessageType() == MessageType.SYSTEM)
                .map(m -> ConversationMessage.builder().role(ApiUtils.toRole(m)).content(m.getContent()).build())
                .toList();
    }

    public static Role toRole(Message message) {
        switch (message.getMessageType()) {
            case USER:
                return Role.USER;
            case ASSISTANT:
                return Role.ASSISTANT;
            case SYSTEM:
                return Role.SYSTEM;
            default:
                throw new IllegalArgumentException("Unsupported message type: " + message.getMessageType());
        }
    }
    public static List<PanguChatMessage> toPanguChatMessage(List<Message> messages){
        List<PanguChatMessage> conversationMessages = new ArrayList<>();
        for (Message message : messages) {
            conversationMessages.add(PanguChatMessage.builder()
                    .role(toRole(message).getText())
                    .content(message.getContent())
                    .build());
        }
        return conversationMessages;
    }

    public static Map<String, Object> toMap(String callBackId, LLMResp llmResp) {
        Map<String, Object> map = new HashMap<>();
        map.put("isFromCache", llmResp.isFromCache());
        map.put("callBackId", callBackId);
        return map;
    }

    public static Map<String, Object> toMap(String id, PanguChatChoice choice) {
        Map<String, Object> map = new HashMap<>();

        var message = choice.getMessage();
        if (message.getRole() != null) {
            map.put("role", message.getRole());
        }
        map.put("finishReason", "");
        map.put("id", id);
        return map;
    }


    public static ChatResponse toChatResponse(PanguChatResp resp) {
        List<Generation> generations = resp.getChoices()
                .stream()
                .map(choice -> new Generation(choice.getMessage().getContent(), ApiUtils.toMap(resp.getId(), choice))
                        .withGenerationMetadata(ChatGenerationMetadata.from("chat.completion", ApiUtils.extractUsage(resp))))
                .toList();
        return new ChatResponse(generations, HuaweiAiPanguChatResponseMetadata.from(resp));
    }

    public static ChatResponse toChatResponse(String callBackId, LLMResp llmResp, boolean completion) {
        List<Generation> generations = Arrays.asList(new Generation(llmResp.getAnswer(), ApiUtils.toMap(callBackId, llmResp))
                .withGenerationMetadata( completion ? ChatGenerationMetadata.from("chat.completion", null) : ChatGenerationMetadata.NULL));
        return new ChatResponse(generations);
    }

    public static Usage extractUsage(PanguChatResp response) {
        return new Usage() {

            @Override
            public Long getPromptTokens() {
                return response.getUsage().getPromptTokens();
            }

            @Override
            public Long getGenerationTokens() {
                return response.getUsage().getCompletionTokens();
            }

            @Override
            public Long getTotalTokens() {
                return response.getUsage().getTotalTokens();
            }
        };
    }

    public static EmbeddingConfig toEmbeddingConfig(HuaweiAiPanguChatTenantOptions tenantOptions) {
        return EmbeddingConfig.builder()
                .iamConfig(toIAMConfig(tenantOptions.getIamOptions()))
                .httpConfig(toHTTPConfig(tenantOptions.getHttpProxyOptions()))
                .build();
    }
}
