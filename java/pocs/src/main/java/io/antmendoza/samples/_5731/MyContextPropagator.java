package io.antmendoza.samples._5731;

import io.temporal.api.common.v1.Payload;
import io.temporal.common.context.ContextPropagator;
import io.temporal.common.converter.DataConverter;
import org.slf4j.MDC;

import java.util.HashMap;
import java.util.Map;

public class MyContextPropagator implements ContextPropagator {

    public MyContextPropagator(final String traceId) {
        MDC.put("traceId", traceId);
    }

    public MyContextPropagator() {
    }

    public String getName() {
        return this.getClass().getName();
    }

    public Object getCurrentContext() {

        Map<String, String> context = new HashMap<>();
        Map<String, String> contextMap = MDC.getCopyOfContextMap();
        Map<String, String> copyOfContextMap = contextMap == null ? new HashMap<>() : contextMap;

        for (Map.Entry<String, String> entry : copyOfContextMap.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            context.put(key, value);
        }
        return context;
    }

    public void setCurrentContext(Object context) {
        Map<String, String> contextMap = (Map<String, String>) context;
        for (Map.Entry<String, String> entry : contextMap.entrySet()) {

            String key = entry.getKey();
            String value = entry.getValue();
            MDC.put(key, value);
        }
    }

    public Map<String, Payload> serializeContext(Object context) {
        Map<String, String> contextMap = (Map<String, String>) context;
        Map<String, Payload> serializedContext = new HashMap<>();
        for (Map.Entry<String, String> entry : contextMap.entrySet()) {
            serializedContext.put(
                    entry.getKey(), DataConverter.getDefaultInstance().toPayload(entry.getValue()).get());
        }
        return serializedContext;
    }

    public Object deserializeContext(Map<String, Payload> context) {
        Map<String, String> contextMap = new HashMap<>();
        for (Map.Entry<String, Payload> entry : context.entrySet()) {
            contextMap.put(
                    entry.getKey(),
                    DataConverter.getDefaultInstance()
                            .fromPayload(entry.getValue(), String.class, String.class));
        }
        return contextMap;
    }
}
