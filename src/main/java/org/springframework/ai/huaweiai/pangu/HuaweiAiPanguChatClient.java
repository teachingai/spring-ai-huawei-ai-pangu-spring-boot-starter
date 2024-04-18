package org.springframework.ai.huaweiai.pangu;

import com.huaweicloud.pangu.dev.sdk.api.callback.StreamCallBack;
import com.huaweicloud.pangu.dev.sdk.client.pangu.PanguClient;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatMessage;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatReq;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.MessageType;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.huaweiai.pangu.metadata.HuaweiAiPanguChatResponseMetadata;
import org.springframework.ai.huaweiai.pangu.util.ApiUtils;
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
    private HuaweiAiPanguChatOptions defaultOptions;
    /**
     * 华为 盘古大模型 LLM library.
     */
    private final PanguClient panguClient;

    private final StreamCallBack streamCallBack;

    private final RetryTemplate retryTemplate;

    public HuaweiAiPanguChatClient(PanguClient panguClient) {
        this(panguClient, HuaweiAiPanguChatOptions.builder()
                        .withTemperature(ApiUtils.DEFAULT_TEMPERATURE)
                        .withTopP(ApiUtils.DEFAULT_TOP_P)
                        .build());
    }

    public HuaweiAiPanguChatClient(PanguClient panguClient, HuaweiAiPanguChatOptions options) {
        this(panguClient, ApiUtils.DEFAULT_STREAM_CALLBACK, options);
    }

    public HuaweiAiPanguChatClient(PanguClient panguClient, StreamCallBack streamCallBack, HuaweiAiPanguChatOptions options) {
        this(panguClient, streamCallBack, options, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiPanguChatClient(PanguClient panguClient,
                                   StreamCallBack streamCallBack,
                                   HuaweiAiPanguChatOptions options,
                                   RetryTemplate retryTemplate) {
        Assert.notNull(panguClient, "PanguClient must not be null");
        Assert.notNull(streamCallBack, "StreamCallBack must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.panguClient = panguClient;
        this.streamCallBack = streamCallBack;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");
        return retryTemplate.execute(ctx -> {
            // Ask the model.
            PanguChatResp panguChatResp;
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                panguChatResp = this.panguClient.createChat(inputContent);
            } else {
                var request = createRequest(prompt, false);
                panguChatResp = this.panguClient.createChat(request);
            }
            if (panguChatResp == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }
            return this.toChatCompletion(panguChatResp) ;
        });
    }


    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {

        Assert.notEmpty(prompt.getInstructions(), "At least one text is required!");

        return retryTemplate.execute(ctx -> {
            // Ask the model.
            PanguChatResp panguChatResp;
            // If there is only one instruction, ask the model by prompt.
            if(prompt.getInstructions().size() == 1){
                var inputContent = CollectionUtils.firstElement(prompt.getInstructions()).getContent();
                panguChatResp = this.panguClient.createStreamChat(inputContent, streamCallBack);
            } else {
                var request = createRequest(prompt, true);
                panguChatResp = this.panguClient.createStreamChat(request, streamCallBack);
            }
            if (panguChatResp == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return Flux.empty();
            }
            return Flux.just(toChatCompletion(panguChatResp)) ;
        })
    }

    private ChatResponse toChatCompletion(PanguChatResp resp) {

        List<Generation> generations = resp.getChoices()
                .stream()
                .map(choice -> new Generation(choice.getMessage().getContent(), ApiUtils.toMap(resp.getId(), choice))
                        .withGenerationMetadata(ChatGenerationMetadata.from("chat.completion", ApiUtils.extractUsage(resp))))
                .toList();

        return new ChatResponse(generations, HuaweiAiPanguChatResponseMetadata.from(resp));
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
