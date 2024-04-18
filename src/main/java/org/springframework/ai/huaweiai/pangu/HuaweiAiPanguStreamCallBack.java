package org.springframework.ai.huaweiai.pangu;

import com.huaweicloud.pangu.dev.sdk.api.callback.StreamCallBack;
import com.huaweicloud.pangu.dev.sdk.api.callback.StreamResult;
import com.huaweicloud.pangu.dev.sdk.api.llms.response.LLMResp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HuaweiAiPanguStreamCallBack implements StreamCallBack {

    private final Logger log = LoggerFactory.getLogger(getClass());

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
