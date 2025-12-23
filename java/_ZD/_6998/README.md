# Workflow execution with mTLS

This example shows how to secure your Temporal application with [mTLS](https://docs.temporal.io/security/#encryption-in-transit-with-mtls).
This is required to connect with Temporal Cloud or any production Temporal deployment.


## Export env variables

Before running the example you need to export the following env variables:

- TEMPORAL_ENDPOINT: grpc endpoint, for Temporal Cloud would like `${namespace.accountId}.tmprl.cloud:7233`.
- TEMPORAL_NAMESPACE: `${namespace.accountId}`
- TEMPORAL_CLIENT_CERT: For Temporal Cloud see requirements [here](https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#end-entity-certificates).
- TEMPORAL_CLIENT_KEY: For Temporal Cloud see requirements [here](https://docs.temporal.io/cloud/how-to-manage-certificates-in-temporal-cloud#end-entity-certificates).

## Running this sample

```bash
./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.ssl.Starter"
```


4.Expected result

The Starter will start a workflow every 2 seconds that will be run and completed by the worker (started as part of the same process)

```text
12:39:17.683 { } [io.temporal.samples.ssl.Starter.main()] INFO  i.t.s.WorkflowServiceStubsImpl - Created WorkflowServiceStubs for channel: ManagedChannelOrphanWrapper{delegate=ManagedChannelImpl{logId=1, target=antonio.a2dd6.tmprl.cloud:7233}} 
12:39:19.957 { } [io.temporal.samples.ssl.Starter.main()] INFO  io.temporal.internal.worker.Poller - start: Poller{name=Workflow Poller taskQueue="MyTaskQueue", namespace="antonio.a2dd6", identity=62379@antmendozas-MacBookPro.local} 
Tue Jul 02 12:39:24 CEST 2024: output done
Tue Jul 02 12:39:27 CEST 2024: output done
Tue Jul 02 12:39:29 CEST 2024: output done
Tue Jul 02 12:39:32 CEST 2024: output done
Tue Jul 02 12:39:35 CEST 2024: output done
Tue Jul 02 12:39:37 CEST 2024: output done
Tue Jul 02 12:39:40 CEST 2024: output done
Tue Jul 02 12:39:42 CEST 2024: output done
Tue Jul 02 12:39:45 CEST 2024: output done
Tue Jul 02 12:39:47 CEST 2024: output done
Tue Jul 02 12:39:50 CEST 2024: output done
Tue Jul 02 12:39:52 CEST 2024: output done
Tue Jul 02 12:39:55 CEST 2024: output done
Tue Jul 02 12:39:57 CEST 2024: output done
Tue Jul 02 12:40:00 CEST 2024: output done
Tue Jul 02 12:40:02 CEST 2024: output done
Tue Jul 02 12:40:05 CEST 2024: output done
Tue Jul 02 12:40:07 CEST 2024: output done
Tue Jul 02 12:40:10 CEST 2024: output done
Tue Jul 02 12:40:13 CEST 2024: output done
Tue Jul 02 12:40:15 CEST 2024: output done
Tue Jul 02 12:40:19 CEST 2024: output done
Tue Jul 02 12:40:21 CEST 2024: output done
Tue Jul 02 12:40:24 CEST 2024: output done
Tue Jul 02 12:40:26 CEST 2024: output done
Tue Jul 02 12:40:29 CEST 2024: output done
Tue Jul 02 12:40:31 CEST 2024: output done
Tue Jul 02 12:40:34 CEST 2024: output done
Tue Jul 02 12:40:36 CEST 2024: output done
Tue Jul 02 12:40:39 CEST 2024: output done
Tue Jul 02 12:40:41 CEST 2024: output done
Tue Jul 02 12:40:44 CEST 2024: output done
Tue Jul 02 12:40:46 CEST 2024: output done
Tue Jul 02 12:40:49 CEST 2024: output done
Tue Jul 02 12:40:51 CEST 2024: output done
Tue Jul 02 12:40:54 CEST 2024: output done
Tue Jul 02 12:40:57 CEST 2024: output done
Tue Jul 02 12:40:59 CEST 2024: output done
Tue Jul 02 12:41:02 CEST 2024: output done
Tue Jul 02 12:41:04 CEST 2024: output done
Tue Jul 02 12:41:07 CEST 2024: output done
Tue Jul 02 12:41:09 CEST 2024: output done
Tue Jul 02 12:41:12 CEST 2024: output done
Tue Jul 02 12:41:14 CEST 2024: output done
Tue Jul 02 12:41:17 CEST 2024: output done
Tue Jul 02 12:41:20 CEST 2024: output done
Tue Jul 02 12:41:23 CEST 2024: output done
Tue Jul 02 12:41:25 CEST 2024: output done
Tue Jul 02 12:41:28 CEST 2024: output done
Tue Jul 02 12:41:30 CEST 2024: output done
Tue Jul 02 12:41:33 CEST 2024: output done
Tue Jul 02 12:41:35 CEST 2024: output done
Tue Jul 02 12:41:38 CEST 2024: output done
Tue Jul 02 12:41:41 CEST 2024: output done
Tue Jul 02 12:41:43 CEST 2024: output done
Tue Jul 02 12:41:46 CEST 2024: output done
Tue Jul 02 12:41:48 CEST 2024: output done
Tue Jul 02 12:41:51 CEST 2024: output done
Tue Jul 02 12:41:53 CEST 2024: output done
Tue Jul 02 12:41:56 CEST 2024: output done
Tue Jul 02 12:41:58 CEST 2024: output done
Tue Jul 02 12:42:01 CEST 2024: output done
Tue Jul 02 12:42:03 CEST 2024: output done
Tue Jul 02 12:42:06 CEST 2024: output done
Tue Jul 02 12:42:08 CEST 2024: output done
Tue Jul 02 12:42:11 CEST 2024: output done
Tue Jul 02 12:42:13 CEST 2024: output done
Tue Jul 02 12:42:16 CEST 2024: output done
Tue Jul 02 12:42:19 CEST 2024: output done
Tue Jul 02 12:42:22 CEST 2024: output done
Tue Jul 02 12:42:24 CEST 2024: output done
Tue Jul 02 12:42:27 CEST 2024: output done
Tue Jul 02 12:42:29 CEST 2024: output done
Tue Jul 02 12:42:32 CEST 2024: output done
Tue Jul 02 12:42:34 CEST 2024: output done
Tue Jul 02 12:42:37 CEST 2024: output done
Tue Jul 02 12:42:40 CEST 2024: output done
Tue Jul 02 12:42:42 CEST 2024: output done
Tue Jul 02 12:42:45 CEST 2024: output done
Tue Jul 02 12:42:47 CEST 2024: output done
Tue Jul 02 12:42:50 CEST 2024: output done
Tue Jul 02 12:42:52 CEST 2024: output done
Tue Jul 02 12:42:55 CEST 2024: output done
Tue Jul 02 12:42:57 CEST 2024: output done
Tue Jul 02 12:43:00 CEST 2024: output done
Tue Jul 02 12:43:02 CEST 2024: output done
Tue Jul 02 12:43:05 CEST 2024: output done
Tue Jul 02 12:43:07 CEST 2024: output done
Tue Jul 02 12:43:10 CEST 2024: output done
Tue Jul 02 12:43:12 CEST 2024: output done
Tue Jul 02 12:43:15 CEST 2024: output done
Tue Jul 02 12:43:17 CEST 2024: output done
Tue Jul 02 12:43:21 CEST 2024: output done
Tue Jul 02 12:43:23 CEST 2024: output done
Tue Jul 02 12:43:26 CEST 2024: output done
Tue Jul 02 12:43:28 CEST 2024: output done
Tue Jul 02 12:43:31 CEST 2024: output done
Tue Jul 02 12:43:33 CEST 2024: output done
Tue Jul 02 12:43:36 CEST 2024: output done
Tue Jul 02 12:43:38 CEST 2024: output done
Tue Jul 02 12:43:41 CEST 2024: output done
Tue Jul 02 12:43:44 CEST 2024: output done
Tue Jul 02 12:43:46 CEST 2024: output done
Tue Jul 02 12:43:49 CEST 2024: output done
Tue Jul 02 12:43:51 CEST 2024: output done
Tue Jul 02 12:43:54 CEST 2024: output done
Tue Jul 02 12:43:56 CEST 2024: output done
Tue Jul 02 12:43:59 CEST 2024: output done
Tue Jul 02 12:44:01 CEST 2024: output done
Tue Jul 02 12:44:04 CEST 2024: output done
Tue Jul 02 12:44:06 CEST 2024: output done
Tue Jul 02 12:44:09 CEST 2024: output done
Tue Jul 02 12:44:11 CEST 2024: output done
Tue Jul 02 12:44:14 CEST 2024: output done
Tue Jul 02 12:44:17 CEST 2024: output done
Tue Jul 02 12:44:20 CEST 2024: output done
Tue Jul 02 12:44:22 CEST 2024: output done
Tue Jul 02 12:44:25 CEST 2024: output done
Tue Jul 02 12:44:28 CEST 2024: output done
Tue Jul 02 12:44:30 CEST 2024: output done

```
