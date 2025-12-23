package com.example.servicespringboot;

import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.opentracingshim.OpenTracingShim;
import io.opentracing.Tracer;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.opentracing.OpenTracingClientInterceptor;
import io.temporal.opentracing.OpenTracingOptions;
import io.temporal.opentracing.OpenTracingWorkerInterceptor;
import io.temporal.spring.boot.TemporalOptionsCustomizer;
import io.temporal.worker.WorkerFactoryOptions;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.annotation.Nonnull;

@Configuration
public class TemporalOptionsConfig {

    @Autowired
    private OpenTelemetry openTelemetry;

    private Tracer tracer;

    @PostConstruct
    public void postConstruct() {
        this.tracer = OpenTracingShim.createTracerShim(openTelemetry);
    }


    // WorkflowClientOption customization
    @Bean
    public TemporalOptionsCustomizer<WorkflowClientOptions.Builder> customClientOptions() {
        return new TemporalOptionsCustomizer<WorkflowClientOptions.Builder>() {
            @Nonnull
            @Override
            public WorkflowClientOptions.Builder customize(
                    @Nonnull WorkflowClientOptions.Builder optionsBuilder) {
                // set options on optionsBuilder as needed
                // ...
                optionsBuilder
                        .setInterceptors(new OpenTracingClientInterceptor(
                                        //        TraceUtils.getOptions()
                                        OpenTracingOptions.newBuilder().setTracer(tracer).build()
                                )
                        );

                return optionsBuilder;
            }
        };
    }


    // WorkflowClientOption customization
    @Bean
    public TemporalOptionsCustomizer<WorkerFactoryOptions.Builder> customWorkerOptions() {
        return new TemporalOptionsCustomizer<WorkerFactoryOptions.Builder>() {
            @Nonnull
            @Override
            public WorkerFactoryOptions.Builder customize(
                    @Nonnull WorkerFactoryOptions.Builder optionsBuilder) {
                // set options on optionsBuilder as needed
                // ...

                optionsBuilder
                        .setWorkerInterceptors(new OpenTracingWorkerInterceptor(
                                        //        TraceUtils.getOptions()
                                        OpenTracingOptions.newBuilder().setTracer(tracer).build()
                                )
                        );

                return optionsBuilder;
            }
        };
    }

}
