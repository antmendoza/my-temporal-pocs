# OpenTelemetry Interceptors

This sample features `@temporalio/interceptors-opentelemetry`, which uses [Interceptors](https://docs.temporal.io/typescript/interceptors) to add tracing of Workflows and Activities with [opentelemetry](https://opentelemetry.io/).

### Running this sample

1. `temporal server start-dev` to start [Temporal Server](https://github.com/temporalio/cli/#installation).
1. `npm install` to install dependencies.
1. `npm run start_worker` to start the Worker for the parent workflow.
1. `npm run start_worker_child` to start the Worker for the child workflow.
1. In another shell, `npm run workflow` to run the Workflow.

traceId is not propagated from parent to child workflow. 