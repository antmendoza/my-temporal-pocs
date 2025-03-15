package com.antmendoza.temporal.config;

import com.sun.net.httpserver.HttpServer;
import com.uber.m3.tally.RootScopeBuilder;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import io.micrometer.core.instrument.config.MeterFilter;
import io.micrometer.core.instrument.logging.LoggingMeterRegistry;
import io.micrometer.prometheus.PrometheusConfig;
import io.micrometer.prometheus.PrometheusMeterRegistry;
import io.temporal.common.reporter.MicrometerClientStatsReporter;

public class ScopeBuilder {

    public Scope create(int port, ImmutableMap<String, String> of) {

        // Set up prometheus registry and stats reported
        PrometheusMeterRegistry registry = new PrometheusMeterRegistry(PrometheusConfig.DEFAULT);
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
