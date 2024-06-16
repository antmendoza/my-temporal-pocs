# POC

## Goal
Inspect workflow histories to suggest improvements in workflow and activity configuration. 


## Implementation
The application has two parts:

### loader package: 
Set of utilities to load and transform Temporal Workflow histories to data that the application can understand and 
manipulate.

#### Relevant classes
  - HistoryLoader: interfaz to implement to load and transform workflow histories
  - WorkflowExecutionHistoryData: WorkflowExecutionHistory files are mapped to an object of this type for ulterior manipulation

### advisor package: 
Engine to inspect WorkflowExecutionHistoryData objects and apply set of rules to generate tips.

#### Relevant classes
  - ConfigurationInspector: interfaz to implement to create a new inspector. See StartToCloseLatencyConfInspector as an example.
  - ConfigurationInspectorFactory: factory that return the ConfigurationInspector classes to apply.
  - WorkflowConfigurationInspector: Returns the list of Tips to apply after applying several ConfigurationInspectors to 
the WorkflowExecutionHistoryData.


## Execute

```bash
mvn compile exec:java -Dexec.mainClass="com.antmendoza.Main"
``` 


