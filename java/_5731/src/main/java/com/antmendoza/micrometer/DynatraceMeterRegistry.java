package com.antmendoza.micrometer;

import io.micrometer.dynatrace.DynatraceApiVersion;
import io.micrometer.dynatrace.DynatraceConfig;

public class DynatraceMeterRegistry {

    public static io.micrometer.dynatrace.DynatraceMeterRegistry get(){


        DynatraceConfig config = new DynatraceConfig() {

            @Override
            public DynatraceApiVersion apiVersion() {
                return DynatraceApiVersion.V2;

            }

            @Override
            public String uri() {
                // The Dynatrace environment URI without any path. For example:
                // https://{your-environment-id}.live.dynatrace.com
                return "https://{id}.live.dynatrace.com/api/v2/metrics/ingest";
            }

            @Override
            public String apiToken() {
                // Should be read from a secure source
                return "token";
            }

            @Override
            public String deviceId() {
                return "MY_DEVICE_ID";
            }

            @Override
            public String get(String k) {
                return null;
            }

        };
        io.micrometer.dynatrace.DynatraceMeterRegistry registry = io.micrometer.dynatrace.DynatraceMeterRegistry.builder(config).build();

        return registry;
    }
}
