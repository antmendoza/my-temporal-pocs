package main

import (
	metrics "_5888"
	"context"
	"go.opentelemetry.io/otel/sdk/instrumentation"
	sdktrace "go.opentelemetry.io/otel/sdk/trace"

	"go.temporal.io/sdk/contrib/opentelemetry"
	"log"
	"time"

	prom "github.com/prometheus/client_golang/prometheus"
	"github.com/uber-go/tally/v4"
	"github.com/uber-go/tally/v4/prometheus"
	"go.temporal.io/sdk/client"
	sdktally "go.temporal.io/sdk/contrib/tally"
	"go.temporal.io/sdk/worker"

	"go.opentelemetry.io/otel"
	"go.opentelemetry.io/otel/exporters/otlp/otlpmetric/otlpmetrichttp"
	"go.opentelemetry.io/otel/exporters/stdout/stdouttrace"
	"go.opentelemetry.io/otel/propagation"
	"go.opentelemetry.io/otel/sdk/metric"
	"go.opentelemetry.io/otel/sdk/resource"

	semconv "go.opentelemetry.io/otel/semconv/v1.17.0"
)

func main() {

	ctx := context.Background()
	exp, err := otlpmetrichttp.New(ctx, otlpmetrichttp.WithEndpointURL("http://localhost:4318"))
	if err != nil {
		panic(err)
	}

	temporalView := metric.NewView(
		metric.Instrument{Name: "temporal*latency*", Scope: instrumentation.Scope{Name: "temporal-sdk-go"}, Kind: metric.InstrumentKindHistogram},
		metric.Stream{
			Aggregation: metric.AggregationExplicitBucketHistogram{
				Boundaries: []float64{
					0.001, 0.01, 0.05, 0.1, 0.2, 0.5, 1, 2, 3, 4, 5, 10, 25, 50, 75, 100,
					200, 300, 400, 500, 1000, 2000, 5000, 10000,
				},
			},
		},
	)

	meterProvider := metric.NewMeterProvider(
		metric.WithReader(metric.NewPeriodicReader(exp, metric.WithInterval(1*time.Second))),
		metric.WithView(temporalView),
	)

	handler := opentelemetry.NewMetricsHandler(
		opentelemetry.MetricsHandlerOptions{
			Meter: meterProvider.Meter("temporal-sdk-go"),
		},
	)

	c, err := client.Dial(client.Options{
		MetricsHandler: handler,
	})

	defer c.Close()

	w := worker.New(c, "metrics", worker.Options{})

	w.RegisterWorkflow(metrics.Workflow)
	w.RegisterActivity(metrics.Activity)

	err = w.Run(worker.InterruptCh())
	if err != nil {
		log.Fatalln("Unable to start worker", err)
	}
}

func newPrometheusScope(c prometheus.Configuration) tally.Scope {
	reporter, err := c.NewReporter(
		prometheus.ConfigurationOptions{
			Registry: prom.NewRegistry(),
			OnError: func(err error) {
				log.Println("error in prometheus reporter", err)
			},
		},
	)
	if err != nil {
		log.Fatalln("error creating prometheus reporter", err)
	}
	scopeOpts := tally.ScopeOptions{
		CachedReporter:  reporter,
		Separator:       prometheus.DefaultSeparator,
		SanitizeOptions: &sdktally.PrometheusSanitizeOptions,
		Prefix:          "temporal_samples",
	}
	scope, _ := tally.NewRootScope(scopeOpts, time.Second)
	scope = sdktally.NewPrometheusNamingScope(scope)

	log.Println("prometheus metrics scope created")
	return scope
}

func InitializeGlobalTracerProvider() (*sdktrace.TracerProvider, error) {
	// Initialize tracer
	exp, err := stdouttrace.New(stdouttrace.WithPrettyPrint())
	if err != nil {
		return nil, err
	}
	tp := sdktrace.NewTracerProvider(
		sdktrace.WithBatcher(exp),
		sdktrace.WithResource(resource.NewWithAttributes(
			semconv.SchemaURL,
			semconv.ServiceName("temporal-example"),
			semconv.ServiceVersion("0.0.1"),
		)),
	)
	otel.SetTracerProvider(tp)

	otel.SetTextMapPropagator(
		propagation.NewCompositeTextMapPropagator(
			propagation.TraceContext{},
			propagation.Baggage{},
		),
	)

	return tp, nil
}
