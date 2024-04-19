package org.springframework.ai.huaweiai.pangu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.pangu.dev.sdk.utils.SecurityUtil;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HuaweiAiPanguModuleOptions {

    /**
     * The URL where Huawei Module API server is running.
     */
    @JsonProperty("url")
    private String url;
    @JsonProperty("moduleVersion")
    private String moduleVersion;
    @JsonProperty("systemPrompt")
    private String systemPrompt;
    @JsonProperty("enableAppendSystemMessage")
    private boolean enableAppendSystemMessage;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final HuaweiAiPanguModuleOptions options = new HuaweiAiPanguModuleOptions();

        public Builder withUrl(String url) {
            this.options.setUrl(url);
            return this;
        }

        public Builder withModuleVersion(String moduleVersion) {
            this.options.setModuleVersion(moduleVersion);
            return this;
        }

        public Builder withSystemPrompt(String systemPrompt) {
            this.options.setSystemPrompt(systemPrompt);
            return this;
        }

        public Builder withEnableAppendSystemMessage(boolean enableAppendSystemMessage) {
            this.options.setEnableAppendSystemMessage(enableAppendSystemMessage);
            return this;
        }

        public HuaweiAiPanguModuleOptions build() {
            return this.options;
        }

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getModuleVersion() {
        return moduleVersion;
    }

    public void setModuleVersion(String moduleVersion) {
        this.moduleVersion = moduleVersion;
    }

    public String getSystemPrompt() {
        return systemPrompt;
    }

    public void setSystemPrompt(String systemPrompt) {
        this.systemPrompt = systemPrompt;
    }

    public boolean isEnableAppendSystemMessage() {
        return enableAppendSystemMessage;
    }

    public void setEnableAppendSystemMessage(boolean enableAppendSystemMessage) {
        this.enableAppendSystemMessage = enableAppendSystemMessage;
    }

    public String getUnionKey() {
        return SecurityUtil.getUnionKey(Objects.toString(this.url),
                Objects.toString(this.moduleVersion),
                Objects.toString(this.systemPrompt),
                Objects.toString(this.enableAppendSystemMessage));
    }

}
