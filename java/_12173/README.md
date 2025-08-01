
# Temporal Java SDK Metrics

See [start-env.md](../start-env.md)

## Configuration

See 
- [temporal.properties](./src/main/resources/temporal.properties) file.
- [env](./.env) file.


## Run the worker

```bash
./start-java-worker.sh

```
Start one worker with the given [env](./.env) variables.


## Start workflows

```bash
./create-backlog.sh
```

The Java dashboard in [dashboard](http://localhost:3000/) will start showing data.
