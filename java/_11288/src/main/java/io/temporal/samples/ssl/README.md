# Workflow execution with mTLS

This example shows how to secure your Temporal application with [mTLS](https://docs.temporal.io/security/#encryption-in-transit-with-mtls).
This is required to connect with Temporal Cloud or any production Temporal deployment.


## Export env variables

Before running the example you need to export the following env variables: 

- TEMPORAL_ENDPOINT: grpc endpoint, for Temporal Cloud would like `${namespace}.tmprl.cloud:7233`.
- TEMPORAL_NAMESPACE: Namespace.
- TEMPORAL_CLIENT_CERT: For Temporal Cloud see requirements [here](https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#end-entity-certificates).
- TEMPORAL_CLIENT_KEY: For Temporal Cloud see requirements [here](https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#end-entity-certificates).

## Running this sample

```bash
./gradlew -q execute -PmainClass=io.temporal.samples.ssl.Starter

```

## Refreshing credentials

- TEMPORAL_CREDENTIAL_REFRESH_PERIOD: The period in seconds to refresh the credentials in minutes.

Setting this env variable will cause the worker to periodically update its credentials. For the full documentation see [here](https://grpc.github.io/grpc-java/javadoc/io/grpc/util/AdvancedTlsX509KeyManager.html).

```bash
# Environment variables
# paths to ca cert, client cert and client key come from the previous step 
export TEMPORAL_CLIENT_CERT="</path/to/client.pem>"
export TEMPORAL_CLIENT_KEY="</path/to/client.key>"
export TEMPORAL_CA_CERT="</path/to/ca.cert>"    
export TEMPORAL_ENDPOINT="localhost:7233"    # Temporal grpc endpoint       
export TEMPORAL_NAMESPACE="default"          # Temporal namespace            
export TEMPORAL_SERVER_HOSTNAME="tls-sample" # Temporal server host name  
```

3.Start the Worker

```bash
./gradlew -q execute -PmainClass="io.temporal.samples.ssl.SslEnabledWorkerCustomCA"
```

4.Expected result

```text
[main] INFO  i.t.s.WorkflowServiceStubsImpl - Created WorkflowServiceStubs for channel: ManagedChannelOrphanWrapper{delegate=ManagedChannelImpl{logId=1, target=localhost:7233}} 
[main] INFO  io.temporal.internal.worker.Poller - start: Poller{name=Workflow Poller taskQueue="MyTaskQueue", namespace="default"} 
Workflow completed:done 
```
