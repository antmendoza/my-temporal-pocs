# Use-case

The application present several screens for the user to submit information. We want to build a system to manage
the navigation between screens, having the following specifications:
- [x] **Prevent race condition:** If two screens (or the same screen) are submitted at the same time, the code will block 
the second and any other incoming requests until the previous one gets processed.
- [x] **Validate request:** The system should be able to validate request and reject request with incomplete data. For this 
example we will reject requests if `data==null`
- [x] **Navigate between screens:** The user can navigate between screens and resubmit the data as many times as needed.
- [x] **Reminders:** Send a reminder to the user if they are inactive for 3 seconds 
    > For test purpose we set the timer to seconds, but it can be minutes, hours, days...


Same implementation in dotnet can be found [here](https://github.com/temporalio/samples-dotnet/blob/49c0c06e6996348aa3f2ebb13fdb62b13d8c6c0a/src/WorkflowUpdate/README.md?plain=1#L1)

## Implementation

We make use of the following Temporal features:

- `Workflow.await`: It allows us to block the current thread until the supplier condition returns true.

  > "block the current thread" does not consume worker/server resources (other than worker cache).

- [Workflow update](https://docs.temporal.io/workflows#update) is one of the Temporal primitives, it  
allows to send messages to a workflow (messages that mutates the workflow state) and have the client waiting for the 
request to complete.

- [Workflow query](https://docs.temporal.io/workflows#query): To get the internal workflow state (eg. instance variable).

- [Timers](https://docs.temporal.io/dev-guide/java/features#timers) //TODO

## How it works

1 - After staring the workflow, the Worker will start running the main workflow method `run` until it gets `#1#`.

`Workflow.await(() -> !data.isEmpty()); // #1#` will block the execution until the supplier condition returns `true`.

2 - The workflow exposes the method `submitScreen` that accepts request. This is an [update method](https://docs.temporal.io/workflows#update) 
that will block the caller until the method returns.

//TODO add timer doc

Once `submitScreen` receives a request:
  - `Workflow.await(() -> this.data.isEmpty()); // #2#`: We use this statement to check if the workflow is not processing other request 
    - if the condition == `false` the execution won't progress (until it eventually becomes `true`) 
    - if/once the condition == `true` the execution will continue to the next line
  - `this.data.add(uiRequest);`: add the request to the list of request to process
  - `Workflow.await(() -> !this.data.contains(uiRequest)); // #3#` will block the execution until the request is processed 

3 - At this point, `#1#` becomes `true`, and the code start processing the request.

4 -`data.remove(uiRequest);` After processing the request, we remove it from the list.

5- and the main method will iterate and go back to evaluate `#1#`

6- the code will process incoming request, one by one, until the submitted screen is the last one. 
At this point the while loop will end and the workflow will complete

Additionally:
- the method `navigateTo` allows the user to navigate from one screen to another. It will persist in a workflow variable the last
screen the user has navigated to.
- the method `submitScreenValidator` will validate the request
before it gets processed by `submitScreen`. If the request is rejected 
it won't be recorded in the Workflow Event History.


## Test

The class [WizardUIWorkflowTest.java](WizardUIWorkflowTest.java) demonstrate how the implementation works. 

It is possible to run the test with a "real" server. To do it, export the following env variable `TEST_LOCALHOST=true` 
(see `WizardUIWorkflowTest.createTestRule` method)