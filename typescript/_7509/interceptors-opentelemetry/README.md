# OpenTelemetry Interceptors

This sample features `@temporalio/interceptors-opentelemetry`, which uses [Interceptors](https://docs.temporal.io/typescript/interceptors) to add tracing of Workflows and Activities with [opentelemetry](https://opentelemetry.io/).


### Start jaeger
   ```sh
   # from this directory
docker run --rm --name jaeger \
  -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
  -p 6831:6831/udp \
  -p 6832:6832/udp \
  -p 5778:5778 \
  -p 16686:16686 \
  -p 4317:4317 \
  -p 4318:4318 \
  -p 14250:14250 \
  -p 14268:14268 \
  -p 14269:14269 \
  -p 9411:9411 \
  jaegertracing/all-in-one:1.59

   ```


### Running this sample

1. `temporal server start-dev` to start [Temporal Server](https://github.com/temporalio/cli/#installation).
1. `npm install` to install dependencies.
1. `npm run start.watch` to start the Worker.
1. In another shell, `npm run workflow` to run the Workflow.


Navigate to http://localhost:16686/trace and select the service `interceptors-sample-worker`

