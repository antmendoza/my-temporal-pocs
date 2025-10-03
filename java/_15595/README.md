Simulate error during replay

- Start the server
```bash
temporal server start-dev
```




- run the worker
- wait until the execution completes
- modify the workflow implementation: add workflow await before invoking the activity in the workflow implementation
- restart the worker
- query the workflow from the UI

![Screenshot 2024-10-03 at 13.18.05.png](Screenshot%202024-10-03%20at%2013.18.05.png)


