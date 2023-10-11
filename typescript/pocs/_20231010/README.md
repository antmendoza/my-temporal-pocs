This image is not meant to be used in production

- add your client certificates into `/cert`
- modify the `Dockerfile` changing TEMPORAL_NAMESPACE and TEMPORAL_ADDRESS
- build and start the worker: `docker build  -t my-worker . &&  docker run  --rm my-worker`
- start the workflow: open a new terminal window
  - export the following environment variables:
```    
    export TEMPORAL_ADDRESS=<namespaceId>.tmprl.cloud:7233
    export TEMPORAL_NAMESPACE=<namespaceId>
    export TEMPORAL_CLIENT_CERT_PATH=./certs/client.pem
    export TEMPORAL_CLIENT_KEY_PATH=./certs/client.key
```
  - run `npm run worklfow` 




Expected output:
```
2023-10-10T21:28:00.053Z [INFO] Workflow bundle created { size: '0.73MB' }
2023-10-10T21:28:00.185041Z  INFO temporal_sdk_core::worker: Initializing worker task_queue=hello-world-mtls namespace=antonio.a2dd6
Worker connection successfully established
2023-10-10T21:28:00.186Z [INFO] Worker state changed { state: 'RUNNING' }
```
