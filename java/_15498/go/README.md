### Steps to run this sample:


Export the following environment variables (change the values as needed):
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
3) Run the following command to configure the schedule
   ```bash
   go run schedule/main.go -target-host $TARGET_HOST -namespace $NAMESPACE -client-cert $CLIENT_CERT -client-key $CLIENT_KEY
   ```

5) Run the following command to start the remote codec server.
   ```bash
   go run ./codec-server 
   ```

6) Go to Temporal Cloud UI, 
- configure the codec endpoint for the namespace `http://localhost:8081/default`, 
- and see that the payloads are decoded (uncompressed) in the UI for the workflow started by the schedule.


![Screenshot 2025-09-21 at 22.33.50.png](Screenshot%202025-09-21%20at%2022.33.50.png)


7) Edit the schedule in the UI to change the input parameters:

![Screenshot 2025-09-21 at 22.30.02.png](Screenshot%202025-09-21%20at%2022.30.02.png)


8) json/protobuf Encoding is not selected by default. 
- Select it and set `codecserver.Input`
- Change the input parameters, for example:

![Screenshot 2025-09-21 at 22.37.47.png](Screenshot%202025-09-21%20at%2022.37.47.png)


9) Trigger the schedule manually from the UI and see that the workflow started by the schedule has the updated input parameters:

![Screenshot 2025-09-21 at 22.38.30.png](Screenshot%202025-09-21%20at%2022.38.30.png)

![Screenshot 2025-09-21 at 22.39.30.png](Screenshot%202025-09-21%20at%2022.39.30.png)


-----


## Other useful commands

A protobuf schema for the workflow input is defined at `proto/input.proto` (message `Input` with fields `input1` and `input2`). If you have `protoc-gen-go` installed, you can generate Go types with:

   ```bash
   protoc --go_out=. proto/input.proto
   ```




To run the starter use the following command:
   ```bash
   go run starter/main.go -target-host $TARGET_HOST -namespace $NAMESPACE -client-cert $CLIENT_CERT -client-key $CLIENT_KEY
   ```


Run the following command to see that the CLI can now decode (uncompress) the payloads via the remote codec server
   ```bash
   temporal workflow show -w codecserver_workflowID --codec-endpoint http://localhost:8081/default
   ```