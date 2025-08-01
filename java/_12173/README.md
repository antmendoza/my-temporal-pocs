
## MaxActivityTasksPerSecond is not reset to the default value if the value is not send by the worker

When a worker start, and it set `MaxActivityTasksPerSecond` to x, the activity dispatch rate is applying correctly. 

If the worker restart without setting `MaxActivityTasksPerSecond`, [max_tasks_per_second](https://github.com/temporalio/api/blob/d96bd55e87799e9f6a33a1c40a56cfa932566bdf/temporal/api/taskqueue/v1/message.proto#L33) sent in PollActivityTaskQueueRequest is not present, and the dispatch rate is not reset to the default value 
in the server (I believe here https://github.com/temporalio/temporal/blob/61679595fff5a3ceab087e13ee45109a35c2f546/service/matching/task_queue_partition_manager.go#L389)


If the value is not set, the server should reset the dispatch rate to the default value




