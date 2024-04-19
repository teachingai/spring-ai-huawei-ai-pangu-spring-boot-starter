package org.springframework.ai.huaweiai.pangu;

import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguClient;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatMessage;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatReq;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatResp;
import com.huaweicloud.pangu.dev.sdk.exception.PanguDevSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.huaweiai.pangu.util.ApiUtils;
import org.springframework.ai.huaweiai.pangu.util.LlmUtils;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Objects;

public class HuaweiAiPanguChatClient implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());

    /**
     * Default options to be used for all chat requests.
     */
    private final HuaweiAiPanguChatOptions defaultOptions;
    /**
     * 华为 盘古大模型 LLM library.
     */
    private final PanguClient panguClient;

    private final RetryTemplate retryTemplate;

    public HuaweiAiPanguChatClient(PanguClient panguClient) {
        this(panguClient, HuaweiAiPanguChatOptions.builder()
                        .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                        .withTopP(ApiUtils.DEFAULT_TOP_P)
                        .build());
    }

    public HuaweiAiPanguChatClient(PanguClient panguClient, HuaweiAiPanguChatOptions options) {
        this(panguClient, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiPanguChatClient(PanguClient panguClient,
                                   HuaweiAiPanguChatOptions options,
                                   RetryTemplate retryTemplate) {
        Assert.notNull(panguClient, "PanguClient must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.defaultOptions = options;
        this.panguClient = panguClient;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        // execute the request
        return retryTemplate.execute(ctx -> {
            Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");
            // Use tenant specific client if available.
            PanguClient llmClient;
            if(prompt.getOptions() != null && prompt.getOptions() instanceof HuaweiAiPanguChatTenantOptions chatOptions){
                llmClient = LlmUtils.getOrCreatePanguClient(chatOptions)
                        .orElseThrow(() -> new PanguDevSDKException("PanguClient initialization failed for Tenant Request."));
            } else {
                llmClient = this.panguClient;
            }
            // Ask the model.
            PanguChatResp panguChatResp;
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                panguChatResp = llmClient.createChat(inputContent);
            } else {
                var request = createRequest(prompt, false);

                var chatCompletionMessages = prompt.getInstructions()
                        .stream()
                        .filter(message -> message.getMessageType() == MessageType.USER
                                || message.getMessageType() == MessageType.ASSISTANT
                                || message.getMessageType() == MessageType.SYSTEM)
                        .map(m -> PanguChatMessage.builder().role(ApiUtils.toRole(m).getText()).content(m.getContent()).build())
                        .toList();
                llmClient.createChat(chatCompletionMessages);
                panguChatResp = llmClient.createChat(request);
            }
            if (panguChatResp == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }
            return ApiUtils.toChatResponse(panguChatResp);
        });
    }


    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {
        // execute the request
        return retryTemplate.execute(ctx -> Flux.create(sink -> {
            Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");
            // Use tenant specific client if available.
            PanguClient llmClient;
            if(prompt.getOptions() != null && prompt.getOptions() instanceof HuaweiAiPanguChatTenantOptions chatOptions){
                llmClient = LlmUtils.getOrCreatePanguClient(chatOptions)
                        .orElseThrow(() -> new PanguDevSDKException("PanguClient initialization failed for Tenant Request."));
            } else {
                llmClient = this.panguClient;
            }
            // Ask the model.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                llmClient.createStreamChat(inputContent, new HuaweiAiPanguStreamCallBack(sink));
            } else {
                var request = createRequest(prompt, true);
                llmClient.createStreamChat(request, new HuaweiAiPanguStreamCallBack(sink));
            }
        }));
    }

    /**
     * Accessible for testing.
     */
    PanguChatReq createRequest(Prompt prompt, boolean stream) {

        // Build PanguChatMessage list from the prompt.
        var chatCompletionMessages = prompt.getInstructions()
                .stream()
                .filter(message -> message.getMessageType() == MessageType.USER
                        || message.getMessageType() == MessageType.ASSISTANT
                        || message.getMessageType() == MessageType.SYSTEM)
                .map(m -> PanguChatMessage.builder().role(ApiUtils.toRole(m).getText()).content(m.getContent()).build())
                .toList();

        // runtime options
        HuaweiAiPanguChatOptions runtimeOptions = null;
        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeChatOptions) {
                runtimeOptions = ModelOptionsUtils.copyToTarget(runtimeChatOptions, ChatOptions.class, HuaweiAiPanguChatOptions.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: " + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Merge runtime options with default options.
        HuaweiAiPanguChatOptions mergedOptions = ModelOptionsUtils.merge(runtimeOptions, this.defaultOptions, HuaweiAiPanguChatOptions.class);

        // Build the PanguChatReq.
        return PanguChatReq.builder()
                .answerNum(mergedOptions.getAnswerNum())
                .maxTokens(mergedOptions.getMaxTokens())
                .messages(chatCompletionMessages)
                .temperature(Objects.nonNull(mergedOptions.getTemperature()) ? mergedOptions.getTemperature().doubleValue() : null)
                .topP(Objects.nonNull(mergedOptions.getTopP()) ? mergedOptions.getTopP().doubleValue() : null)
                .presencePenalty(mergedOptions.getPresencePenalty())
                .user(mergedOptions.getUser())
                .withPrompt(mergedOptions.getWithPrompt())
                .isStream(stream)
                .build();
    }

}
