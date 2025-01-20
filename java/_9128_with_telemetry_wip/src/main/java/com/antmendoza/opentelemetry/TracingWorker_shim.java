/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.antmendoza.opentelemetry;

import com.antmendoza.opentelemetry.workflow.TracingWorkflowImpl;
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
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;
import io.opentracing.util.GlobalTracer;
import io.temporal.client.WorkflowClient;
import io.temporal.opentracing.OpenTracingWorkerInterceptor;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.worker.WorkerFactoryOptions;
import io.temporal.worker.WorkerOptions;
import java.util.concurrent.TimeUnit;

public class TracingWorker_shim {
  public static final String TASK_QUEUE_NAME = "tracingTaskQueue";

  private static final WorkflowServiceStubs service =
      WorkflowServiceStubs.newServiceStubs(WorkflowServiceStubsOptions.newBuilder().build());
  private static final WorkflowClient client = WorkflowClient.newInstance(service);

  public static void main(String[] args) {

    Resource serviceNameResource =
        Resource.create(
            Attributes.of(ResourceAttributes.SERVICE_NAME, "temporal-sample-opentelemetry"));

    OtlpGrpcSpanExporter exporter =
        OtlpGrpcSpanExporter.builder().setTimeout(1, TimeUnit.SECONDS).build();

    SdkTracerProvider tracerProvider =
        SdkTracerProvider.builder()
            .addSpanProcessor(BatchSpanProcessor.builder(exporter).build())
            .setResource(Resource.getDefault().merge(serviceNameResource))
            .build();

    OpenTelemetrySdk openTelemetry =
        OpenTelemetrySdk.builder()
            .setPropagators(
                ContextPropagators.create(
                    TextMapPropagator.composite(W3CTraceContextPropagator.getInstance())))
            // .setTracerProvider(tracerProvider)
            .build();

    // Set the OpenTracing client interceptor
    WorkerFactoryOptions factoryOptions =
        WorkerFactoryOptions.newBuilder()
            .setWorkerInterceptors(new OpenTracingWorkerInterceptor())
            .build();
    WorkerFactory factory = WorkerFactory.newInstance(client, factoryOptions);

    io.opentracing.Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);
    // or io.opentracing.Tracer tracer = OpenTracingShim.createTracerShim(openTelemetry);
    GlobalTracer.registerIfAbsent(tracer);

    final int executors = 100;
    Worker worker =
        factory.newWorker(
            TASK_QUEUE_NAME,
            WorkerOptions.newBuilder()
                .setMaxConcurrentWorkflowTaskExecutionSize(executors)
                .setMaxConcurrentActivityExecutionSize(executors)
                .setMaxConcurrentLocalActivityExecutionSize(executors)
                .build());
    worker.registerWorkflowImplementationTypes(TracingWorkflowImpl.class);

    factory.start();
  }
}
