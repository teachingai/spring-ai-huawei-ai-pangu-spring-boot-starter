package org.springframework.ai.pangu.autoconfigure;

import com.huaweicloud.pangu.dev.sdk.api.llms.LLMs;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(PanguAiConnectionProperties.CONFIG_PREFIX)
public class PanguAiConnectionProperties {

    public static final String CONFIG_PREFIX = "spring.ai.llm";

    private LLMName llmName = LLMName.PANGU;
    private String accessKey;
    private String secretKey;


    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public enum LLMName {
        PANGU(LLMs.PANGU),
        OPENAI(LLMs.OPENAI),
        GALLERY(LLMs.OPENAI);

        private String value;
        LLMName(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
