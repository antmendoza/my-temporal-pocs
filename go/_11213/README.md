# Interceptor Sample

This sample shows how to make a worker interceptor that intercepts workflow and activity `GetLogger` calls to customize
the logger.

### Steps to run this sample:
- Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).


- Install dependencies

```bash
go mod tidy
```


- Run the following command to start the worker

```bash
go run ./worker
```


- Run the following command to start the example

```bash
go run ./starter
```

https://github.com/temporalio/sdk-go/blob/9d74a905fc3602dfa9dddf114087c43a1b64e6b8/internal/interceptor.go#L69

