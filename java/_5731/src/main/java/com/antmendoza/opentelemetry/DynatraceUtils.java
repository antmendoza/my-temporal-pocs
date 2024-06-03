package com.antmendoza.opentelemetry;

import com.antmendoza.EnvVariables;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.export.SimpleSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentracing.Tracer;
import io.temporal.opentracing.OpenTracingOptions;
import io.temporal.opentracing.OpenTracingSpanContextCodec;

import java.util.concurrent.TimeUnit;

public class DynatraceUtils {

    public static OpenTracingOptions getDynatraceOptions(String type) {
        if (type.equals("OpenTracing")) {
            return getDynatraceOpenTracingOptions();
        }
        // default to Open Telemetry
        return getDynatraceOpenTelemetryOptions();
    }


    private static OpenTracingOptions getDynatraceOpenTracingOptions() {
        Resource serviceNameResource =
                Resource.create(
                        Attributes.of(ResourceAttributes.SERVICE_NAME, "temporal-sample-opentelemetry"));

        OtlpGrpcSpanExporter dynatraceExporter =
                OtlpGrpcSpanExporter.builder()
                        //TODO: Replace with general endpoint and DT Access Token
                        //.setEndpoint("http://localhost:14250") //default http://localhost:4317"
                        //.addHeader("Authorization", EnvVariables.getDynatraceApiToken())
                        .setTimeout(1, TimeUnit.SECONDS)
                        .build();


        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(SimpleSpanProcessor.create(dynatraceExporter))
                        .setResource(Resource.getDefault().merge(serviceNameResource))
                        .build();

        OpenTelemetrySdk openTelemetry =
                OpenTelemetrySdk.builder()
                        .setPropagators(
                                ContextPropagators.create(
                                        TextMapPropagator.composite(
                                                W3CTraceContextPropagator.getInstance())))
                        .setTracerProvider(tracerProvider)
                        .build();

        // create OpenTracing shim and return OpenTracing Tracer from it
        return getOpenTracingOptionsForTracer(OpenTracingShim.createTracerShim(openTelemetry));
    }

    private static OpenTracingOptions getDynatraceOpenTelemetryOptions() {
        Resource serviceNameResource =
                Resource.create(
                        //TODO: Replace this with name of service variable from Kyma?
                        Attributes.of(ResourceAttributes.SERVICE_NAME, "temporal-sample-opentelemetry"));


        OtlpGrpcSpanExporter dynatraceExporter =
                OtlpGrpcSpanExporter.builder()
                        //TODO: Replace with general endpoint and DT Access Token
                        .setEndpoint(EnvVariables.getOtelEndpoint())
                        .addHeader("Authorization", EnvVariables.getDynatraceApiToken())
                        .setTimeout(1, TimeUnit.SECONDS)
                        .build();


        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        .addSpanProcessor(BatchSpanProcessor.builder(dynatraceExporter).build())
                        .setResource(Resource.getDefault().merge(serviceNameResource))
                        .build();

        OpenTelemetrySdk openTelemetry =
                OpenTelemetrySdk.builder()
                        .setPropagators(
                                ContextPropagators.create(
                                        TextMapPropagator.composite(W3CTraceContextPropagator.getInstance())))
                        .setTracerProvider(tracerProvider)
                        .build();

        // create OpenTracing shim and return OpenTracing Tracer from it
        return getOpenTracingOptionsForTracer(OpenTracingShim.createTracerShim(openTelemetry));
    }

    private static OpenTracingOptions getOpenTracingOptionsForTracer(Tracer tracer) {
        OpenTracingOptions options =
                OpenTracingOptions.newBuilder()
                        .setSpanContextCodec(OpenTracingSpanContextCodec.TEXT_MAP_CODEC)
                        .setTracer(tracer)
                        .build();
        return options;
    }
}