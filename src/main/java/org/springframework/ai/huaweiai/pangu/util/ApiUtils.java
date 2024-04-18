package org.springframework.ai.huaweiai.pangu.util;

import com.huaweicloud.pangu.dev.sdk.api.callback.StreamCallBack;
import com.huaweicloud.pangu.dev.sdk.api.config.HTTPConfig;
import com.huaweicloud.pangu.dev.sdk.api.config.IAMConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMModuleConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMParamConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.ConversationMessage;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.Role;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatChoice;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatMessage;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatResp;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguChatOptions;
import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguStreamCallBack;
import org.springframework.ai.huaweiai.pangu.autoconfigure.HuaweiAiPanguConnectionProperties;
import org.springframework.ai.huaweiai.pangu.autoconfigure.HuaweiAiPanguHttpProxyProperties;
import org.springframework.ai.huaweiai.pangu.autoconfigure.HuaweiAiPanguIamProperties;

import java.util.*;

public class ApiUtils {

    public static final StreamCallBack DEFAULT_STREAM_CALLBACK = new HuaweiAiPanguStreamCallBack();
    public static final Float DEFAULT_TEMPERATURE = 0.95f;
    public static final Float DEFAULT_TOP_P = 1.0f;

    public static LLMModuleConfig toLLMModuleConfig(HuaweiAiPanguConnectionProperties connectionProperties){
        LLMModuleConfig httpConfig = LLMModuleConfig.builder()
                .LLMName(LLMs.PANGU)
                .url(connectionProperties.getBaseUrl())
                .moduleVersion(connectionProperties.getModuleVersion())
                .enableAppendSystemMessage(connectionProperties.isEnableAppendSystemMessage())
                .systemPrompt(connectionProperties.getSystemPrompt())
                .build();
        return httpConfig;
    }

    public static LLMParamConfig toLLMParamConfig(HuaweiAiPanguChatOptions options){
        LLMParamConfig llmParamConfig = LLMParamConfig.builder()
                .maxTokens(options.getMaxTokens())
                .temperature(Objects.nonNull(options.getTemperature()) ?  options.getTemperature().doubleValue() : null)
                .topP(Objects.nonNull(options.getTopK()) ?  options.getTopK().doubleValue() : null)
                .presencePenalty(options.getPresencePenalty())
                .frequencyPenalty(options.getFrequencyPenalty())
                .bestOf(options.getBestOf())
                .withPrompt(options.getWithPrompt())
                .build();
        return llmParamConfig;
    }

    public static IAMConfig toIAMConfig(HuaweiAiPanguIamProperties iamProperties){
        IAMConfig iamConfig = IAMConfig.builder()
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
        return iamConfig;
    }

    public static HTTPConfig toHTTPConfig(HuaweiAiPanguHttpProxyProperties proxyProperties){
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

}
