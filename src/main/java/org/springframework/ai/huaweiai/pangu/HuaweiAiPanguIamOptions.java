package org.springframework.ai.huaweiai.pangu;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.huaweicloud.pangu.dev.sdk.utils.SecurityUtil;

import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class HuaweiAiPanguIamOptions {

    /**
     * The URL where Huawei IAM API server is running.
     */
    @JsonProperty("url")
    private String url;
    /**
     * The domain of the IAM API server.
     */
    @JsonProperty("domain")
    private String domain;
    /**
     * The project name for the IAM API server.
     */
    @JsonProperty("project_name")
    private String projectName;
    /**
     * The user name for the IAM API server.
     */
    @JsonProperty("user")
    private String user;
    /**
     * The password for the IAM API server.
     */
    @JsonProperty("password")
    private String password;
    /**
     * The access key for the IAM API server.
     */
    @JsonProperty("ak")
    private String ak;
    /**
     * The secret key for the IAM API server.
     */
    @JsonProperty("sk")
    private String sk;

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {

        private final HuaweiAiPanguIamOptions options = new HuaweiAiPanguIamOptions();

        public Builder withUrl(String url) {
            this.options.setUrl(url);
            return this;
        }

        public Builder withDomain(String domain) {
            this.options.setDomain(domain);
            return this;
        }

        public Builder withProjectName(String projectName) {
            this.options.setProjectName(projectName);
            return this;
        }

        public Builder withUser(String user) {
            this.options.setUser(user);
            return this;
        }

        public Builder withPassword(String password) {
            this.options.setPassword(password);
            return this;
        }

        public Builder withAk(String ak) {
            this.options.setAk(ak);
            return this;
        }

        public Builder withSk(String sk) {
            this.options.setSk(sk);
            return this;
        }

        public HuaweiAiPanguIamOptions build() {
            return this.options;
        }

    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getAk() {
        return ak;
    }

    public void setAk(String ak) {
        this.ak = ak;
    }

    public String getSk() {
        return sk;
    }

    public void setSk(String sk) {
        this.sk = sk;
    }

    public String getUnionKey() {
        return SecurityUtil.getUnionKey(Objects.toString(this.url),
                Objects.toString(this.domain),
                Objects.toString(this.projectName),
                Objects.toString(this.user),
                Objects.toString(this.ak),
                Objects.toString(this.sk));
    }

}
