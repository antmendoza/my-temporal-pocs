# Overview

OpenTelemetry Mongodb Instrumentation allows the user to automatically collect trace data and export them to the backend of choice (we can use Zipkin or Jaeger for this example), to give observability to distributed systems.

This is a modification of the Mongo example that executes multiple parallel requests that interact with a Mongodb server backend using the `mongo` npm module. The example displays traces using multiple connection methods.

- Create Collection Query
- Insert Document Query
- Fetch All Documents Query

## Installation

```sh
# from this directory
npm install
```

Setup [Zipkin Tracing](https://zipkin.io/pages/quickstart.html)

   ```sh
   # from this directory
  docker run -d -p 9411:9411 openzipkin/zipkin
   ```


or
Setup [Jaeger Tracing](https://www.jaegertracing.io/docs/latest/getting-started/#all-in-one)



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

## Run the Application

### Zipkin

- Start MongoDB server in docker

   ```sh
   # from this directory
   npm run docker:start
   ```

- Run the server

   ```sh
   # from this directory
   npm run zipkin:server
   ```

- Run the client

   ```sh
   # from this directory
   npm run zipkin:client
   ```

#### Zipkin UI

After a short time, the generated traces should be available in the Zipkin UI.
Visit <http://localhost:9411/zipkin> and click the "RUN QUERY" button to view
recent traces, then click "SHOW" on a given trace.

<p align="center"><img alt="Zipkin UI with trace" src="./images/zipkin.png?raw=true"/></p>

### Jaeger

- Start MongoDB server via docker

   ```sh
   # from this directory
   npm run docker:start
   ```

- Run the server

   ```sh
   # from this directory
   npm run jaeger:server
   ```

- Run the client

   ```sh
   # from this directory
   npm run jaeger:client
   ```

#### Jaeger UI

Visit the Jaeger UI at <http://localhost:16686/search>, select a service (e.g. "example-express-client"), click "Find Traces", then click on a trace to view it.

<p align="center"><img alt="Jaeger UI with trace" src="images/jaeger-ui.png?raw=true"/></p>

## Useful links

- For more information on OpenTelemetry, visit: <https://opentelemetry.io/>
- For more information on OpenTelemetry for Node.js, visit: <https://github.com/open-telemetry/opentelemetry-js/tree/main/packages/opentelemetry-sdk-trace-node>

## LICENSE

Apache License 2.0
