


```bash
poetry install
```


``` bash
poetry run python3 worker.py 

```

```bash
poetry run python3 starter.py 

```


## Send cancellation request after 4 seconds with default activity options 
- in [worker.py](worker.py)
```
    return await workflow.execute_activity(
        ...
        ...
        # Tell the workflow to wait for the post-cancel result
        # cancellation_type=workflow.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED,
    )

```


**logs / output**

```
Heartbeating ...
activity running ...0
activity running ...1
Heartbeating ...
activity running ...2
Heartbeating ...
activity running ...3
Heartbeating ...
activity running ...4
Heartbeating ...


```


![canceled_after_4_seconds_with_default.png](canceled_after_4_seconds_with_default.png)



## Send cancellation request after 4 seconds having activity options with cancellation_type=workflow.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED

- in [worker.py](worker.py)

```
  return await workflow.execute_activity(
      ......
      # Tell the workflow to wait for the post-cancel result
      cancellation_type=workflow.ActivityCancellationType.WAIT_CANCELLATION_COMPLETED,

  )
  
 
```
**logs / output**

```

Heartbeating ...
activity running ...0
Heartbeating ...
activity running ...1
Heartbeating ...
activity running ...2
Heartbeating ...
activity running ...3
Heartbeating ...
activity running ...4
Heartbeating ...

```

![canceled_after_4_seconds_with_WAIT_CANCELLATION_COMPLETED.png](canceled_after_4_seconds_with_WAIT_CANCELLATION_COMPLETED.png)



## Run everything without sending a cancellation request 

- in [starter.py](starter.py)

```
# await client.get_workflow_handle(workflow_id).cancel();

```
**logs / output**

```

Heartbeating ...
activity running ...0
Heartbeating ...
activity running ...1
Heartbeating ...
activity running ...2
Heartbeating ...
activity running ...3
activity running ...4
Heartbeating ...
activity running ...5
Heartbeating ...
activity running ...6
Heartbeating ...
activity running ...7
Heartbeating ...
activity running ...8
Heartbeating ...
Heartbeating ...
activity running ...9
Heartbeating ...

```


![activity_completed_without_cancellation_request.png](activity_completed_without_cancellation_request.png)

