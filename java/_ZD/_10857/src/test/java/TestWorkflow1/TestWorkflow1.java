package TestWorkflow1;

import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;

@WorkflowInterface
public interface TestWorkflow1 {
    @WorkflowMethod
    String execute(String taskQueue);
}
