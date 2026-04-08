### Steps to run this sample:


1) Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).


2) 
```bash
go mod tidy
```


3) Run the following command to start the worker X 2
```bash
go run worker/main.go \
-target-host antonio.a2dd6.tmprl.cloud:7233 -namespace antonio.a2dd6 \
-client-cert /Users/antmendoza/dev/temporal/certificates/namespace-antonio-perez/client.pem \
-client-key /Users/antmendoza/dev/temporal/certificates/namespace-antonio-perez/client.key

```


4) Run the following command to start the example
```bash
go run starter/main.go \
-target-host antonio.a2dd6.tmprl.cloud:7233 -namespace antonio.a2dd6 \
-client-cert /Users/antmendoza/dev/temporal/certificates/namespace-antonio-perez/client.pem \
-client-key /Users/antmendoza/dev/temporal/certificates/namespace-antonio-perez/client.key

```

