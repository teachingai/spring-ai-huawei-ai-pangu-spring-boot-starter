package org.springframework.ai.huaweiai.pangu.aot;

import org.springframework.ai.huaweiai.pangu.HuaweiAiPanguChatOptions;
import org.springframework.aot.hint.MemberCategory;
import org.springframework.aot.hint.RuntimeHints;
import org.springframework.aot.hint.RuntimeHintsRegistrar;

import static org.springframework.ai.aot.AiRuntimeHints.findJsonAnnotatedClassesInPackage;

public class HuaweiAiPanguRuntimeHints implements RuntimeHintsRegistrar {

    @Override
    public void registerHints(RuntimeHints hints, ClassLoader classLoader) {
        var mcs = MemberCategory.values();
        for (var tr : findJsonAnnotatedClassesInPackage(HuaweiAiPanguChatOptions.class)) {
            hints.reflection().registerType(tr, mcs);
        }
    }

}
