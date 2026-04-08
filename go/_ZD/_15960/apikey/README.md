### Steps to run this sample:
1) Configure a [Temporal Server](https://github.com/temporalio/samples-go/tree/main/#how-to-use) (such as Temporal Cloud) with apiKey.

2) Run the following command to start the worker
``` bash
go run ./worker \
    -target-host us-west-2.aws.api.temporal.io:7233 \
    -namespace antonio.a2dd6 \
    -api-key CLIENT_API_KEY
```
3) Run the following command to start the example
``` bash
go run ./starter \
    -target-host us-west-2.aws.api.temporal.io:7233 \
    -namespace antonio.a2dd6 \
    -api-key CLIENT_API_KEY
```