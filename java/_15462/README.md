# First workflow task execution latency 

- Open `temporal.properties to set the configuration

- Run the following command to start the worker and run the sample:

```bash

./mvnw compile exec:java -Dexec.mainClass="io.temporal.samples.earlyreturn.EarlyReturnRunner" 

```

- Worker 1 shuts down; its sticky queue has no pollers.
- The workflow still has stickiness set (the SDK sent sticky attributes on the prior WFT completion), so History tries the sticky path first.
- Sticky dispatch fails quickly (no sticky poller). History issues ResetStickyTaskQueue, clearing stickiness for the run.
  - https://github.com/temporalio/temporal/blob/16b1e29b2a86eaf1a95e026e49e16f31f8c414aa/service/history/api/queryworkflow/api.go#L348
- History then calls Matching QueryWorkflow on the normal queue (“HelloActivityTaskQueue”).
  - https://github.com/temporalio/temporal/blob/16b1e29b2a86eaf1a95e026e49e16f31f8c414aa/service/history/api/queryworkflow/api.go#L383-L392
- Worker 2 (polling normal queue) replays and answers the query, which you see in RespondQueryTaskCompleted with the normal task queue in the token.
