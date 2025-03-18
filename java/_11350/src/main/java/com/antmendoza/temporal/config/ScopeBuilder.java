package com.antmendoza.temporal.config;

import com.sun.net.httpserver.HttpServer;
import com.uber.m3.tally.RootScopeBuilder;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import io.micrometer.core.instrument.Meter;
import io.micrometer.core.instrument.Tag;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.config.MeterFilterReply;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.temporal.common.reporter.MicrometerClientStatsReporter;

import java.util.List;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

public class ScopeBuilder {


    public Scope create(int port, ImmutableMap<String, String> of) {

        // Set up prometheus registry and stats reported
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
        registry
                .config()
                .meterFilter(
                new MeterFilter() {
                    @Override
                    public Meter.Id map(Meter.Id id) {
                        if(!id.getName().contains("my_metric_name")){
                            return id;
                        }
                        List<Tag> tags = id.getTags().stream().filter(t -> !t.getKey().equals("node_id")).toList();
                        return id.replaceTags(tags);
                    }
                });




        // Set up a new scope, report every 1 second
        Scope scope =
                new RootScopeBuilder()
                        // shows how to set custom tags
                        .tags(
                                of)
                        .reporter(new MicrometerClientStatsReporter(registry))
                        .reportEvery(com.uber.m3.util.Duration.ofSeconds(1));
        // Start the prometheus scrape endpoint

        HttpServer scrapeEndpoint = MetricsUtils.startPrometheusScrapeEndpoint(registry, port);
        // Stopping the worker will stop the http server that exposes the
        // scrape endpoint.
        Runtime.getRuntime().addShutdownHook(new Thread(() -> scrapeEndpoint.stop(1)));


        return scope;
    }
}
