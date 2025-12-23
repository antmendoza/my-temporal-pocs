package com.antmendoza.micrometer;

import com.antmendoza.EnvVariables;
import io.micrometer.dynatrace.DynatraceApiVersion;
import io.micrometer.dynatrace.DynatraceConfig;

public class DynatraceMeterRegistry {

    public static io.micrometer.dynatrace.DynatraceMeterRegistry get(){


        DynatraceConfig config = new DynatraceConfig() {

            @Override
            public DynatraceApiVersion apiVersion() {
                return DynatraceApiVersion.V1;

            }

            @Override
            public String uri() {
                // The Dynatrace environment URI without any path. For example:
                return EnvVariables.getOtelEndpoint();
            }

            @Override
            public String apiToken() {
                // Should be read from a secure source
                return EnvVariables.getDynatraceApiToken();
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
