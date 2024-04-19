package org.springframework.ai.huaweiai.pangu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.pangu.dev.sdk.utils.SecurityUtil;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HuaweiAiPanguHttpProxyOptions {

    /**
     * If the http proxy is enabled.
     */
    @JsonProperty("proxyEnabled")
    private Boolean proxyEnabled;
    /**
     * The URL of the http proxy.
     */
    @JsonProperty("proxyUrl")
    private String proxyUrl;
    /**
     * The username for the http proxy.
     */
    @JsonProperty("proxyUser")
    private String proxyUser;
    /**
     * The password for the http proxy.
     */
    @JsonProperty("proxyPassword")
    private String proxyPassword;
    /**
     * The number of seconds to wait for the async http request.
     */
    @JsonProperty("asyncHttpWaitSeconds")
    private Integer asyncHttpWaitSeconds;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final HuaweiAiPanguHttpProxyOptions options = new HuaweiAiPanguHttpProxyOptions();

        public Builder withProxyEnabled(Boolean proxyEnabled) {
            this.options.setProxyEnabled(proxyEnabled);
            return this;
        }

        public Builder withProxyUrl(String proxyUrl) {
            this.options.setProxyUrl(proxyUrl);
            return this;
        }

        public Builder withProxyUser(String proxyUser) {
            this.options.setProxyUser(proxyUser);
            return this;
        }

        public Builder withProxyPassword(String proxyPassword) {
            this.options.setProxyPassword(proxyPassword);
            return this;
        }

        public Builder withAsyncHttpWaitSeconds(Integer asyncHttpWaitSeconds) {
            this.options.setAsyncHttpWaitSeconds(asyncHttpWaitSeconds);
            return this;
        }

        public HuaweiAiPanguHttpProxyOptions build() {
            return this.options;
        }

    }

    public Boolean getProxyEnabled() {
        return proxyEnabled;
    }

    public void setProxyEnabled(Boolean proxyEnabled) {
        this.proxyEnabled = proxyEnabled;
    }

    public String getProxyUrl() {
        return proxyUrl;
    }

    public void setProxyUrl(String proxyUrl) {
        this.proxyUrl = proxyUrl;
    }

    public String getProxyUser() {
        return proxyUser;
    }

    public void setProxyUser(String proxyUser) {
        this.proxyUser = proxyUser;
    }

    public String getProxyPassword() {
        return proxyPassword;
    }

    public void setProxyPassword(String proxyPassword) {
        this.proxyPassword = proxyPassword;
    }

    public Integer getAsyncHttpWaitSeconds() {
        return asyncHttpWaitSeconds;
    }

    public void setAsyncHttpWaitSeconds(Integer asyncHttpWaitSeconds) {
        this.asyncHttpWaitSeconds = asyncHttpWaitSeconds;
    }

    public String getUnionKey() {
        return SecurityUtil.getUnionKey(Objects.toString(this.proxyEnabled),
                Objects.toString(this.proxyUrl),
                Objects.toString(this.proxyUser),
                Objects.toString(this.proxyPassword),
                Objects.toString(this.asyncHttpWaitSeconds));
    }
}
