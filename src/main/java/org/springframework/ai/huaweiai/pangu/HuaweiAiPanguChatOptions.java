package org.springframework.ai.huaweiai.pangu;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.model.function.FunctionCallback;
import org.springframework.ai.model.function.FunctionCallingOptions;

import java.util.List;
import java.util.Map;
import java.util.Set;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HuaweiAiPanguChatOptions implements FunctionCallingOptions, ChatOptions {

    /**
     * 指定模型最大输出token数
     */
    @JsonProperty("max_tokens")
    private Integer maxTokens;

    /**
     * 采样温度，控制输出的随机性，必须为正数
     * 取值范围是：(0.0, 1.0)，不能等于 0，默认值为 0.95，值越大，会使输出更随机，更具创造性；值越小，输出会更加稳定或确定
     * 建议您根据应用场景调整 top_p 或 temperature 参数，但不要同时调整两个参数
     * 较高的数值会使输出更加随机，而较低的数值会使其更加集中和确定，范围 (0, 1.0]，不能为0
     */
    @JsonProperty("temperature")
    private Float temperature;

    /**
     * 用温度取样的另一种方法，称为核取样取值范围是：(0.0, 1.0) 开区间，不能等于 0 或 1，默认值为 0.7
     * 模型考虑具有 top_p 概率质量 tokens 的结果
     * 例如：0.1 意味着模型解码器只考虑从前 10% 的概率的候选集中取 tokens
     * 建议您根据应用场景调整 top_p 或 temperature 参数，但不要同时调整两个参数
     */
    @JsonProperty("top_p")
    private Float topP;

    /**
     * 通过对已生成的token增加惩罚，减少重复生成的现象。说明：值越大表示惩罚越大，取值范围：[1.0, 2.0]
     */
    @JsonProperty(value = "penaltyScore")
    private Double penaltyScore;

    /**
     * 模型人设，主要用于人设设定
     */
    @JsonProperty(value = "system")
    private String system;

    /**
     * 终端用户的唯一ID，协助平台对终端用户的违规行为、生成违法及不良信息或其他滥用行为进行干预。ID长度要求：最少6个字符，最多128个字符。
     */
    @JsonProperty(value = "user")
    private String user;

    /**
     * 生成停止标识，当模型生成结果以stop中某个元素结尾时，停止文本生成
     */
    @JsonProperty("stop")
    private List<String> stop;

    @JsonProperty("answerNum")
    private Integer answerNum;

    /**
     * 存在惩罚，增加模型谈论新主题的可能性; 范围见具体模型API规范；
     */
    @JsonProperty(value = "presencePenalty")
    private Double presencePenalty;
    /**
     * 频率惩罚，降低模型重复的可能性，提高文本多样性、创造型; 范围见具体模型API规范；
     */
    @JsonProperty(value = "frequencyPenalty")
    private Double frequencyPenalty;
    /**
     * 指定响应内容的格式
     */
    @JsonProperty(value = "withPrompt")
    private Boolean withPrompt;
    /**
     * 服务侧生成优选的回答数
     */
    @JsonProperty(value = "bestOf")
    private Integer bestOf;

    @Override
    public List<FunctionCallback> getFunctionCallbacks() {
        return null;
    }

    @Override
    public void setFunctionCallbacks(List<FunctionCallback> functionCallbacks) {

    }

    @Override
    public Set<String> getFunctions() {
        return null;
    }

    @Override
    public void setFunctions(Set<String> functions) {

    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final HuaweiAiPanguChatOptions options = new HuaweiAiPanguChatOptions();

        public Builder withMaxToken(Integer maxTokens) {
            this.options.setMaxTokens(maxTokens);
            return this;
        }

        public Builder withTemperature(Float temperature) {
            this.options.setTemperature(temperature);
            return this;
        }

        public Builder withTopP(Float topP) {
            this.options.setTopP(topP);
            return this;
        }

        public Builder withPenaltyScore(Double penaltyScore) {
            this.options.setPenaltyScore(penaltyScore);
            return this;
        }

        public Builder withSystem(String system) {
            this.options.setSystem(system);
            return this;
        }

        public Builder withUser(String user) {
            this.options.setUser(user);
            return this;
        }

        public Builder withStop(List<String> stop) {
            this.options.setStop(stop);
            return this;
        }

        public Builder withAnswerNum(Integer answerNum) {
            this.options.setAnswerNum(answerNum);
            return this;
        }

        public Builder withPresencePenalty(Double presencePenalty) {
            this.options.setPresencePenalty(presencePenalty);
            return this;
        }

        public Builder withFrequencyPenalty(Double frequencyPenalty) {
            this.options.setFrequencyPenalty(frequencyPenalty);
            return this;
        }

        public Builder withWithPrompt(Boolean withPrompt) {
            this.options.setWithPrompt(withPrompt);
            return this;
        }

        public Builder withBestOf(Integer bestOf) {
            this.options.setBestOf(bestOf);
            return this;
        }

        public HuaweiAiPanguChatOptions build() {
            return this.options;
        }

    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    @Override
    public Float getTemperature() {
        return this.temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    @Override
    public Float getTopP() {
        return this.topP;
    }

    public void setTopP(Float topP) {
        this.topP = topP;
    }

    public Integer getAnswerNum() {
        return answerNum;
    }

    public void setAnswerNum(Integer answerNum) {
        this.answerNum = answerNum;
    }

    @Override
    @JsonIgnore
    public Integer getTopK() {
        throw new UnsupportedOperationException("Unimplemented method 'getTopK'");
    }

    @JsonIgnore
    public void setTopK(Integer topK) {
        throw new UnsupportedOperationException("Unimplemented method 'setTopK'");
    }

    public Double getPenaltyScore() {
        return penaltyScore;
    }

    public void setPenaltyScore(Double penaltyScore) {
        this.penaltyScore = penaltyScore;
    }

    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public List<String> getStop() {
        return stop;
    }

    public void setStop(List<String> stop) {
        this.stop = stop;
    }

    public Double getPresencePenalty() {
        return presencePenalty;
    }

    public void setPresencePenalty(Double presencePenalty) {
        this.presencePenalty = presencePenalty;
    }

    public Double getFrequencyPenalty() {
        return frequencyPenalty;
    }

    public void setFrequencyPenalty(Double frequencyPenalty) {
        this.frequencyPenalty = frequencyPenalty;
    }

    public Boolean getWithPrompt() {
        return withPrompt;
    }

    public void setWithPrompt(Boolean withPrompt) {
        this.withPrompt = withPrompt;
    }

    public Integer getBestOf() {
        return bestOf;
    }

    public void setBestOf(Integer bestOf) {
        this.bestOf = bestOf;
    }

    /**
     * Convert the {@link HuaweiAiPanguChatOptions} object to a {@link Map} of key/value pairs.
     * @return The {@link Map} of key/value pairs.
     */
    public Map<String, Object> toMap() {
        try {
            var json = new ObjectMapper().writeValueAsString(this);
            return new ObjectMapper().readValue(json, new TypeReference<Map<String, Object>>() {
            });
        }
        catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

}
