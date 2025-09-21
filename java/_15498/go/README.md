### Steps to run this sample:

This sample shows how to decode payloads that have been encoded by a codec so they can be displayed by the Temporal CLI and Temporal UI.
The sample codec server supports OIDC authentication (via JWT in the Authorization header).
Temporal UI can be configured to pass the user's OIDC access token to the codec server, see: https://docs.temporal.io/references/web-ui-configuration#auth


```bash  
export NAMESPACE=antonio.a2dd6
export TARGET_HOST=antonio.a2dd6.tmprl.cloud:7233

export CLIENT_CERT=./client.pem
export CLIENT_KEY=./client.key
```


1) Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).
2) Run the following command to start the worker
   ```bash
   go run worker/main.go -target-host $TARGET_HOST -namespace $NAMESPACE -client-cert $CLIENT_CERT -client-key $CLIENT_KEY
   ```
3) Run the following command to start the example
   ```bash

   go run starter/main.go -target-host $TARGET_HOST -namespace $NAMESPACE -client-cert $CLIENT_CERT -client-key $CLIENT_KEY
   ```

5) Run the following command to start the remote codec server.
   The `-web` flag is needed for Temporal UI for CORS.
   ```bash
   go run ./codec-server 
   ```

6) Run the following command to see that the CLI can now decode (uncompress) the payloads via the remote codec server
   ```bash
   temporal workflow show -w codecserver_workflowID --codec-endpoint http://localhost:8081/default
   ```


Note: A protobuf schema for the workflow input is defined at `proto/input.proto` (message `Input` with fields `input1` and `input2`). If you have `protoc-gen-go` installed, you can generate Go types with:

   ```bash
   protoc --go_out=. proto/input.proto
   ```
