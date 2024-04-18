package org.springframework.ai.huaweiai.pangu.autoconfigure;

public class HuaweiAiPanguHttpProxyProperties {

    private Boolean proxyEnabled;
    private String proxyUrl;
    private String proxyUser;
    private String proxyPassword;
    private Integer asyncHttpWaitSeconds;

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
}
