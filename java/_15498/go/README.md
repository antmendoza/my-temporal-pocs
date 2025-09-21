### Steps to run this sample:

This sample shows how to decode payloads that have been encoded by a codec so they can be displayed by the Temporal CLI and Temporal UI.
The sample codec server supports OIDC authentication (via JWT in the Authorization header).
Temporal UI can be configured to pass the user's OIDC access token to the codec server, see: https://docs.temporal.io/references/web-ui-configuration#auth
Configuring OIDC is outside the scope of this sample, but please see the [serverjwtauth repo](../serverjwtauth/) for more details about authentication.

1) Run a [Temporal service](https://github.com/temporalio/samples-go/tree/main/#how-to-use).
2) Run the following command to start the worker
   ```bash
   go run worker/main.go
   ```
3) Run the following command to start the example
   ```bash
   go run starter/main.go
   ```
5) Run the following command to start the remote codec server.
   The `-web` flag is needed for Temporal UI for CORS.
   ```bash
   go run ./codec-server -web=http://localhost:8080
   ```
6) Run the following command to see that the CLI can now decode (uncompress) the payloads via the remote codec server
   ```bash
   temporal workflow show -w codecserver_workflowID --codec-endpoint http://localhost:8081/default
   ```
