package org.springframework.ai.huaweiai.pangu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HuaweiAiPanguChatTenantOptions extends HuaweiAiPanguChatOptions {

    /**
     * IAM 配置，用于接口运行期间动态修改 IAM 信息，方便支持多租户场景
     */
    @JsonProperty(value = "iamOptions")
    private HuaweiAiPanguIamOptions iamOptions;
    /**
     * 模型配置，用于接口运行期间动态修改模型信息，方便支持多租户场景
     */
    @JsonProperty(value = "moduleOptions")
    private HuaweiAiPanguModuleOptions moduleOptions;
    /**
     * 网络代理配置，用于接口运行期间动态修改网络代理信息，方便支持多租户场景
     */
    @JsonProperty(value = "httpProxyOptions")
    private HuaweiAiPanguHttpProxyOptions httpProxyOptions;

    public static TenantBuilder tenantBuilder() {
        return new TenantBuilder();
    }

    public static class TenantBuilder {

        private HuaweiAiPanguChatTenantOptions options;

        public TenantBuilder() {
            this.options = new HuaweiAiPanguChatTenantOptions();
        }

        public TenantBuilder withMaxToken(Integer maxTokens) {
            this.options.setMaxTokens(maxTokens);
            return this;
        }

        public TenantBuilder withTemperature(Float temperature) {
            this.options.setTemperature(temperature);
            return this;
        }

        public TenantBuilder withTopP(Float topP) {
            this.options.setTopP(topP);
            return this;
        }

        public TenantBuilder withPenaltyScore(Double penaltyScore) {
            this.options.setPenaltyScore(penaltyScore);
            return this;
        }

        public TenantBuilder withSystem(String system) {
            this.options.setSystem(system);
            return this;
        }

        public TenantBuilder withUser(String user) {
            this.options.setUser(user);
            return this;
        }

        public TenantBuilder withStop(List<String> stop) {
            this.options.setStop(stop);
            return this;
        }

        public TenantBuilder withAnswerNum(Integer answerNum) {
            this.options.setAnswerNum(answerNum);
            return this;
        }

        public TenantBuilder withPresencePenalty(Double presencePenalty) {
            this.options.setPresencePenalty(presencePenalty);
            return this;
        }

        public TenantBuilder withFrequencyPenalty(Double frequencyPenalty) {
            this.options.setFrequencyPenalty(frequencyPenalty);
            return this;
        }

        public TenantBuilder withWithPrompt(Boolean withPrompt) {
            this.options.setWithPrompt(withPrompt);
            return this;
        }

        public TenantBuilder withBestOf(Integer bestOf) {
            this.options.setBestOf(bestOf);
            return this;
        }

        public TenantBuilder withStream(Boolean stream) {
            this.options.setStream(stream);
            return this;
        }

        public TenantBuilder withIamOptions(HuaweiAiPanguIamOptions iamOptions) {
            this.options.setIamOptions(iamOptions);
            return this;
        }

        public TenantBuilder withModuleOptions(HuaweiAiPanguModuleOptions moduleOptions) {
            this.options.setModuleOptions(moduleOptions);
            return this;
        }

        public TenantBuilder withHttpProxyOptions(HuaweiAiPanguHttpProxyOptions httpProxyOptions) {
            this.options.setHttpProxyOptions(httpProxyOptions);
            return this;
        }

        public HuaweiAiPanguChatTenantOptions build() {
            return this.options;
        }

    }

    public HuaweiAiPanguIamOptions getIamOptions() {
        return iamOptions;
    }

    public HuaweiAiPanguModuleOptions getModuleOptions() {
        return moduleOptions;
    }

    public void setModuleOptions(HuaweiAiPanguModuleOptions moduleOptions) {
        this.moduleOptions = moduleOptions;
    }

    public void setIamOptions(HuaweiAiPanguIamOptions iamOptions) {
        this.iamOptions = iamOptions;
    }

    public HuaweiAiPanguHttpProxyOptions getHttpProxyOptions() {
        return httpProxyOptions;
    }

    public void setHttpProxyOptions(HuaweiAiPanguHttpProxyOptions httpProxyOptions) {
        this.httpProxyOptions = httpProxyOptions;
    }

}
