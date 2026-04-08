### Steps to run this sample:
- Configure a [Temporal Server](https://github.com/temporalio/samples-go/tree/main/#how-to-use) (such as Temporal Cloud) with mTLS.

- Run the following command to start the worker

``` bash  
go run ./worker -target-host antonio.a2dd6.tmprl.cloud:7233 -namespace antonio.a2dd6 -client-cert client.pem -client-key client.key          
```


- Run the following command to start the example


``` bash
go run ./starter -target-host antonio.a2dd6.tmprl.cloud:7233 -namespace antonio.a2dd6 -client-cert client.pem -client-key client.key
```
