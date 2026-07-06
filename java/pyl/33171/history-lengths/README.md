# history-lengths

Reads a workflow-list JSON export from this ticket folder and, for each distinct workflow id,
lists all matching executions in the namespace, fetches each run's history, and prints the
history length (event count).

## Configure

Edit `config.properties` (gitignored):

```
endpoint=us-west-2.aws.api.temporal.io:7233
namespace=pay-orchestration-prod.g66lv
apiKey=<your-api-key>
```

The API key can instead be supplied via env: `export TEMPORAL_API_KEY=...` (takes precedence).

## Run

```bash
# input JSON is the first arg; config.properties is picked up by default
mvn -q compile exec:java -Dexec.args="../stuck_workflows_workflows-108-1-1782992054661.json"

# explicit config path as second arg:
mvn -q compile exec:java -Dexec.args="../stuck_workflows_workflows-108-1-1782992054661.json config.properties"
```

## Output

```
WORKFLOW_ID                               RUN_ID                                STATUS          EVENTS
--------------------------------------------------------------------------------------------------------------
delayed-capture-dk-1593611419             bf2e8450-b22c-48df-a9e4-4ab9d72716ab  RUNNING         42
...
```

