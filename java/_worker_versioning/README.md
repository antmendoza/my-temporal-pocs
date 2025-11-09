# Temporal 


```bash

temporal server start-dev 

```

## Example 1

- start a workflow without any reference to worker versioning
- start the worker without any reference/configuration for worker versioning
  - the worker start processing the workflow tasks
- start another worker, same task queue, setting up worker versioning to loan-proces:v1 and autoupgrade
  - the second worker starts processing new workflow tasks


## Example 2


## AUTO UPGRADE 


- run [Runner_test_2.java](src/main/java/io/temporal/samples/hello/Runner_test_2.java)
- run [Runner_test_3.java](src/main/java/io/temporal/samples/hello/Runner_test_3.java)


- run [Client_auto_upgrade.java](src/main/java/io/temporal/samples/hello/Client_auto_upgrade.java)

Set current version
```bash
temporal worker deployment set-current-version --deployment-name "test" --version "test.2" -y
    
```

- workflow started before should start making progress



Ramping up to a new version
```bash

temporal worker deployment set-ramping-version \
--deployment-name "test" \
--version "test.3" \
--percentage=5 -y

```


- run [Client_auto_upgrade_100_wf.java](src/main/java/io/temporal/samples/hello/Client_auto_upgrade_100_wf.java)

- some workflows will be started with the new version

Set current version 1.3
```bash
temporal worker deployment set-current-version --deployment-name "test" --version "test.3" -y
    
```

All workflows (running workflows) will be moved to the new version


## PINNED WORKFLOWS

WIP