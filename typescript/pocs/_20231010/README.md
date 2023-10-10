This image is not meant to be used in production

- add your client certificates into `/cert`
- modify the `Dockerfile` changing TEMPORAL_NAMESPACE and TEMPORAL_ADDRESS
- run `docker build  -t my-worker . &&  docker run  --rm my-worker`


Expected output:
```
2023-10-10T21:28:00.053Z [INFO] Workflow bundle created { size: '0.73MB' }
2023-10-10T21:28:00.185041Z  INFO temporal_sdk_core::worker: Initializing worker task_queue=hello-world-mtls namespace=antonio.a2dd6
Worker connection successfully established
2023-10-10T21:28:00.186Z [INFO] Worker state changed { state: 'RUNNING' }
```
