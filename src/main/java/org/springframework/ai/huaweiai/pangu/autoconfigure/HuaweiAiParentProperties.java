package org.springframework.ai.huaweiai.pangu.autoconfigure;


class HuaweiAiParentProperties {

    /**
     * Base URL where Huawei 盘古大模型 AI API server is running.
     */
    private String baseUrl;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

}
