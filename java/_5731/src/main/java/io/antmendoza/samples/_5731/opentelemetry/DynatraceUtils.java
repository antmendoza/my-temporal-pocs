package io.antmendoza.samples._5731.opentelemetry;

import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapPropagator;
import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentracing.Tracer;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.temporal.opentracing.OpenTracingOptions;
import io.temporal.opentracing.OpenTracingSpanContextCodec;

import java.util.concurrent.TimeUnit;

public class DynatraceUtils {

  public static OpenTracingOptions getDynatraceOptions(String type) {
    if (type.equals("OpenTracing")) {
     // return getDynatraceOpenTracingOptions();
    }
    // default to Open Telemetry
    return getDynatraceOpenTelemetryOptions();
  }

  private static OpenTracingOptions getDynatraceOpenTracingOptions() {
    // Dynatrace does not support OpenTracing directly. So, using OpenTelemetry.
    return getDynatraceOpenTelemetryOptions();
  }

  private static OpenTracingOptions getDynatraceOpenTelemetryOptions() {
    Resource serviceNameResource =
            Resource.create(
                    //TODO: Replace this with name of service variable from Kyma?
                    Attributes.of(ResourceAttributes.SERVICE_NAME, "temporal-sample-opentelemetry"));

    final String value = "token";


    OtlpGrpcSpanExporter dynatraceExporter =
            OtlpGrpcSpanExporter.builder()
                    //TODO: Replace with general endpoint and DT Access Token
                    .setEndpoint("https://{id}.live.dynatrace.com/api/v2/metrics/ingest")
                    .addHeader("Authorization", value)
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