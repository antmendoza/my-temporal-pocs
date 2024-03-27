<p align="center">
  <a href="http://nestjs.com/" target="blank"><img src="https://nestjs.com/img/logo-small.svg" width="120" alt="Nest Logo" /><a>
</p>

## Description

[Nest](https://github.com/nestjs/nest) framework sample with the [Temporal TypeScript SDK](https://github.com/temporalio/sdk-typescript).

This sample app is based on this [blog post about caching API requests with long-lived Workflows](https://temporal.io/blog/caching-api-requests-with-long-lived-workflows).

## Installation

```bash
$ npm install
```

## Running the app

1. `temporal server start-dev` to start [Temporal Server](https://github.com/temporalio/cli/#installation).
2. `npm run worker` to start the Worker.
3. `npm run start` to start the NestJS server.
4. `npm run client` to start start a workflow.
5. `` start the server to handle activity requests 

6. 
7. Visit `http://localhost:3000/exchange-rates/AUD` to see the most recent exchange rate for AUD (Australian Dollar)
