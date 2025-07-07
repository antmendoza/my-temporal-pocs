# 

```bash
    uv python install 3.13
```

```bash
    uv sync
```



```bash
    uv run starter.py
```


 Separate terminal:

```bash
    uv run worker.py
```


```
WARNING:temporalio.worker.workflow_sandbox._restrictions:__mro_entries__ on concurrent.futures.Executor restricted
WARNING:temporalio.worker._workflow_instance:Failed activation on workflow ExecutorRestrictedWorkflow with ID context-propagation-workflow-id and run ID 0197e446-9632-7d81-a827-6a82352169a1
Traceback (most recent call last):
...
```
