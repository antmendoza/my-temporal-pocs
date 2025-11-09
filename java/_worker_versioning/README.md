# Temporal 


```bash

temporal server start-dev 

```

## Example 1: from unversioned to versioned worker with auto upgrade

- start a workflow
- start an unversioned worker 
  - the worker start processing the workflow tasks
- start versioned worker, same task queue, setting up worker versioning to `loan-proces<random>:v1` and `autoupgrade`
  - the previous worker continues processing existing workflow tasks
- promote `loan-proces<random>:v1` to current version
  - the second worker starts processing new workflow tasks

```
New Workflows: New Workflows are automatically directed to the Current Version.
Auto-Upgrade Workflows: Auto-Upgrade Workflows are automatically moved to the Current Version when they're eligible for migration, ensuring they can continue running on actively maintained code.
```


## Example 2: auto-upgrade worker versioning
- start a workflow 
- start a versioned worker, setting up worker versioning to `loan-proces<random>:v1` and `autoupgrade`
  - the worker does not process workflow tasks, because current version is not set
- promote `loan-process:<random>:v1` to current version
  - the worker starts processing workflow tasks
- start a second versioned worker, setting up worker versioning to `loan-proces<random>:v2` and `autoupgrade`
  - the previous worker continues processing existing workflow tasks
- promote `loan-proces<random>:v2` to current version
  - the second worker starts processing new workflow tasks



## Example 3: worker versioning with ramping strategy
- start 100 workflows
- start a versioned worker, setting up worker versioning to `loan-proces<random>:v1` and `autoupgrade`
  - the worker does not process workflow tasks, because current version is not set
- promote `loan-process:<random>:v1` to current version
  - the worker starts processing workflow tasks
- start a second versioned worker, setting up worker versioning to `loan-proces<random>:v2` and `autoupgrade`
  - the previous worker continues processing existing workflow tasks
- start ramping `loan-proces<random>:v2` gradually from 0% to 100%
  - the second worker starts processing new workflow tasks based on the ramping percentage
- finally promote `loan-proces<random>:v2` to current version




## Example 4: unversioned worker with ramping strategy
- start 100 workflows
- start an unversioned worker
  - the worker start processing the workflow tasks
- start a second versioned worker, setting up worker versioning to `loan-proces<random>:v1` and `autoupgrade`
  - the previous worker continues processing existing workflow tasks
- start ramping `loan-proces<random>:v1` gradually from 0% to 100%
  - the second worker starts processing new workflow tasks based on the ramping percentage
- finally promote `loan-proces<random>:v1` to current version
