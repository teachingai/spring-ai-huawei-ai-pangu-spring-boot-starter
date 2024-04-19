package org.springframework.ai.huaweiai.pangu;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class HuaweiAiPanguEmbeddingTenantOptions extends HuaweiAiPanguEmbeddingOptions {

    /**
     * Base URL where Huawei 盘古大模型 AI API server is running.
     */
    private String baseUrl;
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

        protected HuaweiAiPanguEmbeddingTenantOptions options;

        public TenantBuilder() {
            this.options = new HuaweiAiPanguEmbeddingTenantOptions();
        }

        public TenantBuilder withBaseUrl(String baseUrl) {
            this.options.setBaseUrl(baseUrl);
            return this;
        }

        public TenantBuilder withModel(String model) {
            this.options.setModel(model);
            return this;
        }

        public TenantBuilder withUser(String user) {
            this.options.setUser(user);
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

        public HuaweiAiPanguEmbeddingTenantOptions build() {
            return this.options;
        }

    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
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
