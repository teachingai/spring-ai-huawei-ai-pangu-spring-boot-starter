package org.springframework.ai.huaweiai.pangu;

import com.huaweicloud.pangu.dev.sdk.api.callback.StreamCallBack;
import com.huaweicloud.pangu.dev.sdk.api.callback.StreamResult;
import com.huaweicloud.pangu.dev.sdk.api.llms.response.LLMResp;
import com.huaweicloud.pangu.dev.sdk.exception.PanguDevSDKException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.ChatResponse;
import org.springframework.ai.huaweiai.pangu.util.ApiUtils;
import reactor.core.publisher.FluxSink;

public class HuaweiAiPanguStreamCallBack implements StreamCallBack {

    private final Logger log = LoggerFactory.getLogger(getClass());

    private FluxSink<ChatResponse> sink;

    public HuaweiAiPanguStreamCallBack(FluxSink<ChatResponse> sink) {
        this.sink = sink;
    }

    @Override
    public void onStart(String callBackId) {
        log.info("StreamCallBack onStart: callBackId ----> {}", callBackId);
    }

    @Override
    public void onEnd(String callBackId, StreamResult streamResult, LLMResp llmResp) {
        log.info("StreamCallBack onEnd: callBackId ----> {} || llmResp ----> {}", callBackId, llmResp);
        sink.next(ApiUtils.toChatResponse(callBackId, llmResp, true));
        sink.complete();
    }

    @Override
    public void onError(String callBackId, StreamResult streamResult) {
        log.error("StreamCallBack onError: callBackId ----> {}", callBackId);
        sink.error(new PanguDevSDKException("Error occurred in stream callback with id: " + callBackId));
    }

    @Override
    public void onNewToken(String callBackId, LLMResp llmResp) {
        log.info("StreamCallBack onNewToken: callBackId ----> {} || llmResp ----> {}", callBackId, llmResp);
         sink.next(ApiUtils.toChatResponse(callBackId, llmResp, false));
    }

}
