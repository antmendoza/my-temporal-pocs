package com.example.service2;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.extension.trace.propagation.OtTracePropagator;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentracing.Tracer;
import io.temporal.opentracing.OpenTracingOptions;
import io.temporal.opentracing.OpenTracingSpanContextCodec;

import java.util.concurrent.TimeUnit;

public class TraceUtils {

    public static OpenTracingOptions getOptions() {



        // default to Open Telemetry
        Resource serviceNameResource =
                Resource.create(
                        Attributes.of(
                                ResourceAttributes.SERVICE_NAME,
                                "temporal-sample-opentelemetry"));


        OtlpGrpcSpanExporter exporter =
                OtlpGrpcSpanExporter.builder()
                        .setTimeout(1, TimeUnit.SECONDS)
                        .build();


        SdkTracerProvider tracerProvider =
                SdkTracerProvider.builder()
                        //.addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
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
        Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);
        OpenTracingOptions options =
                OpenTracingOptions.newBuilder()
                        .setSpanContextCodec(OpenTracingSpanContextCodec.TEXT_MAP_CODEC)
                        .setTracer(tracer)
                        .build();
        return options;
    }


}