# POC

## Goal
Inspect workflow histories to suggest improvements in workflow and activity configuration. 


## Implementation
The application has two main parts

### loader package: 
Set of utilities to load and transform Temporal Workflow histories to data that the application can understand and 
manipulate.

#### Relevant classes
  - [HistoryLoader](./src/main/java/com/antmendoza/loader/HistoryLoader.java): interface to implement to load and transform workflow histories
  - [WorkflowExecutionHistoryData](./src/main/java/com/antmendoza/loader/WorkflowExecutionHistoryData.java): 
WorkflowExecutionHistory files are mapped to an object of this type for ulterior manipulation

### inspector package: 
Engine to inspect [WorkflowExecutionHistoryData](./src/main/java/com/antmendoza/loader/WorkflowExecutionHistoryData.java) 
objects and apply a set of predefined rules to generate tips.

#### Relevant classes
  - [ConfigurationInspector](./src/main/java/com/antmendoza/inspector/ConfigurationInspector.java): interface to implement to create a new inspector. 
See [StartToCloseLatencyConfInspector](./src/main/java/com/antmendoza/StartToCloseLatencyConfInspector.java) as an example.
  - [ConfigurationInspectorFactory](./src/main/java/com/antmendoza/inspector/ConfigurationInspectorFactory.java): factory that returns the list of inspectors to apply.
  - [WorkflowConfigurationInspector](./src/main/java/com/antmendoza/inspector/WorkflowConfigurationInspector.java): Returns the list of Tips 
after applying the list of inspectors (provided by the factory) to the WorkflowExecutionHistoryData object.


## Execute

```bash
mvn compile exec:java -Dexec.mainClass="com.antmendoza.Main"
``` 

**Expected output:** 

```
Result:
ConfigurationInspectorResult{tips=
 Tip{description=[ActivityData{workflowId='workflow_id_in_replay', activityId='a490efe7-fd1f-38fc-a914-7caa325a2422'}]; configurationProperty=[ActivityStartToClose]; actionSuggested=[activityStartToClose configured valued is too high. Set the value to the maximum time the activity execution can take]; configuredValue=[PT2M]; currentValue=[PT0.003S]}
}

```


