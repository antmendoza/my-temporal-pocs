package ctxpropagation

import (
	"github.com/opentracing/opentracing-go"
	"github.com/uber/jaeger-client-go"
	"github.com/uber/jaeger-client-go/config"
	"io"
)

func SetJaegerGlobalTracer() (io.Closer, opentracing.Tracer) {
	cfg := config.Configuration{
		ServiceName: "tracing",
		Sampler: &config.SamplerConfig{
			Type:  jaeger.SamplerTypeConst,
			Param: 1,
		},
		//		Reporter: &config.ReporterConfig{
		//LogSpans: true,
		//		},
	}
	tracer, closer, err := cfg.NewTracer(

	//config.Logger(jaeger.StdLogger),
	)
	if err != nil {
		panic(err)
	}
	opentracing.SetGlobalTracer(tracer)

	return closer, tracer
}
