# Temporal 


```bash
temporal server start-dev \
--dynamic-config-value frontend.workerVersioningWorkflowAPIs=true \
--dynamic-config-value system.enableDeploymentVersions=true
```

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