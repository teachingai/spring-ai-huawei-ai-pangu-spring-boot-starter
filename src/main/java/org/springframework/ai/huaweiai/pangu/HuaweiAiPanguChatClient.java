package org.springframework.ai.huaweiai.pangu;

import com.huaweicloud.pangu.dev.sdk.api.callback.StreamCallBack;
import com.huaweicloud.pangu.dev.sdk.api.callback.StreamResult;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLM;
import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.config.LLMParamConfig;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.ConversationMessage;
import com.huaweicloud.pangu.dev.sdk.api.llms.request.Role;
import com.huaweicloud.pangu.dev.sdk.api.llms.response.LLMResp;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatChoice;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatMessage;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatReq;
import com.huaweicloud.pangu.dev.sdk.client.pangu.chat.PanguChatResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatClient;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.chat.Generation;
import org.springframework.ai.chat.StreamingChatClient;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.metadata.ChatGenerationMetadata;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.model.ModelOptionsUtils;
import org.springframework.ai.model.function.AbstractFunctionCallSupport;
import org.springframework.ai.model.function.FunctionCallbackContext;
import org.springframework.ai.retry.RetryUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class HuaweiAiPanguChatClient
        extends AbstractFunctionCallSupport<PanguChatMessage, PanguChatReq, ResponseEntity<PanguChatResp>>
        implements ChatClient, StreamingChatClient {

    private final Logger log = LoggerFactory.getLogger(getClass());
    /**
     * Default options to be used for all chat requests.
     */
    private HuaweiAiPanguChatOptions defaultOptions;
    /**
     * 华为 盘古大模型 LLM library.
     */
    private final LLM llm;

    private final RetryTemplate retryTemplate;

    public HuaweiAiPanguChatClient(LLM llm) {
        this(llm, HuaweiAiPanguChatOptions.builder()
                        .withTemperature(0.95f)
                        .withTopP(0.7f)
                        //.withModel(ZhipuAiApi.ChatModel.GLM_3_TURBO.getValue())
                        .build());
    }

    public HuaweiAiPanguChatClient(LLM llm, HuaweiAiPanguChatOptions options) {
        this(llm, options, null, RetryUtils.DEFAULT_RETRY_TEMPLATE);
    }

    public HuaweiAiPanguChatClient(LLM llm, HuaweiAiPanguChatOptions options, FunctionCallbackContext functionCallbackContext, RetryTemplate retryTemplate) {
        super(functionCallbackContext);
        Assert.notNull(llm, "LLM must not be null");
        Assert.notNull(options, "Options must not be null");
        Assert.notNull(retryTemplate, "RetryTemplate must not be null");
        this.llm = llm;
        this.defaultOptions = options;
        this.retryTemplate = retryTemplate;
    }

    @Override
    public ChatResponse call(Prompt prompt) {

        var request = createRequest(prompt, false);

        return retryTemplate.execute(ctx -> {

            ResponseEntity<PanguChatResp> completionEntity = this.callWithFunctionSupport(request);

            var chatCompletion = completionEntity.getBody();
            if (chatCompletion == null) {
                log.warn("No chat completion returned for prompt: {}", prompt);
                return new ChatResponse(List.of());
            }

            List<Generation> generations = chatCompletion.getChoices()
                    .stream()
                    .map(choice -> new Generation(choice.getMessage().getContent(), toMap(chatCompletion.getId(), choice))
                            .withGenerationMetadata(ChatGenerationMetadata.NULL))
                    .toList();

            return new ChatResponse(generations);
        });
    }

    private Map<String, Object> toMap(String id, PanguChatChoice choice) {
        Map<String, Object> map = new HashMap<>();

        var message = choice.getMessage();
        if (message.getRole() != null) {
            map.put("role", message.getRole());
        }
        map.put("finishReason", "");
        map.put("id", id);
        return map;
    }

    @Override
    protected PanguChatReq doCreateToolResponseRequest(PanguChatReq previousRequest, PanguChatMessage responseMessage, List<PanguChatMessage> conversationHistory) {
        return null;
    }

    @Override
    protected List<PanguChatMessage> doGetUserMessages(PanguChatReq request) {
        return null;
    }

    @Override
    protected PanguChatMessage doGetToolResponseMessage(ResponseEntity<PanguChatResp> response) {
        return null;
    }

    @Override
    protected ResponseEntity<PanguChatResp> doChatCompletion(PanguChatReq request) {
        return null;
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<PanguChatResp> response) {
        return false;
    }

    public class StreamCallBackImp implements StreamCallBack {
        @Override
        public void onStart(String callBackId) {
            log.info("StreamCallBack onStart: callBackId ----> {}", callBackId);
        }

        @Override
        public void onEnd(String callBackId, StreamResult streamResult, LLMResp llmResp) {
            log.info("StreamCallBack onEnd: callBackId ----> {} || llmResp ----> {}", callBackId, llmResp);
        }

        @Override
        public void onError(String callBackId, StreamResult streamResult) {
            log.error("StreamCallBack onError: callBackId ----> {}", callBackId);
        }

        @Override
        public void onNewToken(String callBackId, LLMResp llmResp) {
            log.info("StreamCallBack onNewToken: callBackId ----> {} || llmResp ----> {}", callBackId, llmResp);
        }
    }

    // 构造多轮对话：历史问答记录 + 最新问题
    private List<ConversationMessage> buildMultiTurnChatMessages() {
        List<ConversationMessage> messages = new ArrayList<>();
        messages.add(ConversationMessage.builder().role(Role.SYSTEM).content("You are a helpful assistant.").build());
        messages.add(ConversationMessage.builder().role(Role.USER).content("Who won the world series in 2020?").build());
        messages.add(ConversationMessage.builder()
                .role(Role.ASSISTANT)
                .content("The Los Angeles Dodgers won the World Series in 2020.")
                .build());
        messages.add(ConversationMessage.builder().role(Role.USER).content("Where was it played?").build());
        return messages;
    }

    protected Role toRole(Message message) {
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

    protected List<ConversationMessage> toConversationMessage(List<Message> messages){
        List<ConversationMessage> conversationMessages = new ArrayList<>();
        for (Message message : messages) {
            conversationMessages.add(ConversationMessage.builder()
                    .role(toRole(message))
                    .content(message.getContent())
                    .build());
        }
        return conversationMessages;
    }

    @Override
    public Flux<ChatResponse> stream(Prompt prompt) {

        prompt.getInstructions().forEach(instruction -> {
            log.info("instruction: {}", instruction.getContent());
        });



        // 设置模型参数，stream为true
        final LLMConfig llmConfig = LLMConfig.builder().llmParamConfig(LLMParamConfig.builder().stream(true).build()).build();

// 盘古LLM
        LLM pangu = LLMs.of(LLMs.PANGU, llmConfig);
// 设置回调处理逻辑
        pangu.setStreamCallback(new StreamCallBackImp());
        pangu.ask("写一篇200字的散文").getAnswer();
        llm.ask(toConversationMessage(prompt.getInstructions())).getAnswer();

        var request = createRequest(prompt, true);

        return retryTemplate.execute(ctx -> {

            var completionChunks = this.zhipuAiApi.chatCompletionStream(request);

            // For chunked responses, only the first chunk contains the choice role.
            // The rest of the chunks with same ID share the same role.
            ConcurrentHashMap<String, String> roleMap = new ConcurrentHashMap<>();

            return completionChunks.map(chunk -> toChatCompletion(chunk)).map(chatCompletion -> {

                chatCompletion = handleFunctionCallOrReturn(request, ResponseEntity.of(Optional.of(chatCompletion)))
                        .getBody();

                @SuppressWarnings("null")
                String id = chatCompletion.id();

                List<Generation> generations = chatCompletion.choices().stream().map(choice -> {
                    if (choice.message().role() != null) {
                        roleMap.putIfAbsent(id, choice.message().role().name());
                    }
                    String finish = (choice.finishReason() != null ? choice.finishReason().name() : "");
                    var generation = new Generation(choice.message().content(),
                            Map.of("id", id, "role", roleMap.get(id), "finishReason", finish));
                    if (choice.finishReason() != null) {
                        generation = generation
                                .withGenerationMetadata(ChatGenerationMetadata.from(choice.finishReason().name(), null));
                    }
                    return generation;
                }).toList();
                return new ChatResponse(generations);
            });
        });
    }

    private ChatResponse toChatCompletion(ChatResponseChunk chunk) {
        List<ChatResponse.Choice> choices = chunk.choices()
                .stream()
                .map(cc -> new ChatResponse.Choice(cc.index(), cc.delta(), cc.finishReason()))
                .toList();

        return new ChatResponse(chunk.id(), "chat.completion", chunk.created(), chunk.model(), choices, null);
    }

    /**
     * Accessible for testing.
     */
    PanguChatReq createRequest(Prompt prompt, boolean stream) {

        Set<String> functionsForThisRequest = new HashSet<>();

        var chatCompletionMessages = prompt.getInstructions()
                .stream()
                .map(m -> new Message(m.getContent(),
                        Message.Role.valueOf(m.getMessageType().name())))
                .toList();

        var request = new ChatRequest(null, chatCompletionMessages, stream);

        if (this.defaultOptions != null) {
            Set<String> defaultEnabledFunctions = this.handleFunctionCallbackConfigurations(this.defaultOptions,
                    !IS_RUNTIME_CALL);

            functionsForThisRequest.addAll(defaultEnabledFunctions);

            request = ModelOptionsUtils.merge(request, this.defaultOptions, ChatRequest.class);
        }

        if (prompt.getOptions() != null) {
            if (prompt.getOptions() instanceof ChatOptions runtimeOptions) {
                var updatedRuntimeOptions = ModelOptionsUtils.copyToTarget(runtimeOptions, ChatOptions.class,
                        HuaweiAiPanguChatOptions.class);

                Set<String> promptEnabledFunctions = this.handleFunctionCallbackConfigurations(updatedRuntimeOptions,
                        IS_RUNTIME_CALL);
                functionsForThisRequest.addAll(promptEnabledFunctions);

                request = ModelOptionsUtils.merge(updatedRuntimeOptions, request,
                        ChatRequest.class);
            }
            else {
                throw new IllegalArgumentException("Prompt options are not of type ChatOptions: "
                        + prompt.getOptions().getClass().getSimpleName());
            }
        }

        // Add the enabled functions definitions to the request's tools parameter.
        if (!CollectionUtils.isEmpty(functionsForThisRequest)) {

            request = ModelOptionsUtils.merge(
                    HuaweiAiPanguChatOptions.builder().withTools(this.getFunctionTools(functionsForThisRequest)).build(),
                    request, ChatRequest.class);
        }

        return request;
    }

    private List<Function> getFunctionTools(Set<String> functionNames) {
        return this.resolveFunctionCallbacks(functionNames).stream().map(functionCallback -> {
            var function = new ZhipuAiApi.FunctionTool.Function(functionCallback.getDescription(),
                    functionCallback.getName(), functionCallback.getInputTypeSchema());
            return new ZhipuAiApi.FunctionTool(function);
        }).toList();
    }

    //
    // Function Calling Support
    //
    @Override
    protected ChatRequest doCreateToolResponseRequest(ChatRequest previousRequest,
                                                      Message responseMessage,
                                                      List<Message> conversationHistory) {

        // Every tool-call item requires a separate function call and a response (TOOL)
        // message.
        for (Message.ToolCall toolCall : responseMessage.toolCalls()) {

            var functionName = toolCall.function().name();
            String functionArguments = toolCall.function().arguments();

            if (!this.functionCallbackRegister.containsKey(functionName)) {
                throw new IllegalStateException("No function callback found for function name: " + functionName);
            }

            String functionResponse = this.functionCallbackRegister.get(functionName).call(functionArguments);

            // Add the function response to the conversation.
            conversationHistory
                    .add(new Message(functionResponse, Message.Role.TOOL, functionName, null));
        }

        // Recursively call chatCompletionWithTools until the model doesn't call a
        // functions anymore.
        ChatRequest newRequest = new ChatRequest(previousRequest.requestId(), conversationHistory, false);
        newRequest = ModelOptionsUtils.merge(newRequest, previousRequest, ChatRequest.class);

        return newRequest;
    }

    @Override
    protected List<Message> doGetUserMessages(ChatRequest request) {
        return request.getMessages();
    }

    @SuppressWarnings("null")
    @Override
    protected Message doGetToolResponseMessage(ResponseEntity<LLMResp> chatCompletion) {
        return chatCompletion.getBody().choices().iterator().next().message();
    }

    @Override
    protected ResponseEntity<ChatResponse> doChatCompletion(ChatRequest request) {


        // 设置模型参数，temperature为0.9
        LLMConfig llmConfig =  LLMConfig.builder().llmParamConfig(LLMParamConfig.builder().temperature(0.9).build()).build();

        // 如使用Gallery三方模型，使用以下配置
        // LLMConfig llmConfig = LLMConfigGallery.builder().llmParamConfig(LLMParamConfig.builder().temperature(0.9).build()).build();

        // 初始化带参数的盘古LLM
        LLM pangu = LLMs.of(LLMs.PANGU, llmConfig);

        llm.ask(request);


        return this.zhipuAiApi.chatCompletionEntity(request);
    }

    @Override
    protected boolean isToolFunctionCall(ResponseEntity<LLMResp> chatCompletion) {
        var body = chatCompletion.getBody();
        if (body == null) {
            return false;
        }
        var choices = body.choices();
        if (CollectionUtils.isEmpty(choices)) {
            return false;
        }

        return !CollectionUtils.isEmpty(choices.get(0).message().toolCalls());
    }
}
