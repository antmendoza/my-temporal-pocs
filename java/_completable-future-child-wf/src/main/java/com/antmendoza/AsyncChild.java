package com.antmendoza;

import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.api.enums.v1.ParentClosePolicy;
import io.temporal.workflow.*;

import java.util.concurrent.CompletableFuture;

public class AsyncChild {

    private final String id;
    private Promise<String> childPromise;
    final CompletablePromise<AsyncChildResult> asyncChildResultCompletablePromise = Workflow.newPromise();

    public AsyncChild(final String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public Promise<AsyncChildResult> start() {


         boolean throwException = true;

        if(this.id.equals("2")){
         throwException = false;
        }

        final WorkflowInput workflowInputChild = new WorkflowInput(
                false,
                throwException);


        final HelloActivityChild.GreetingWorkflowChild child = Workflow.newChildWorkflowStub(HelloActivityChild.GreetingWorkflowChild.class,
                ChildWorkflowOptions.newBuilder()
                        .setParentClosePolicy(ParentClosePolicy.PARENT_CLOSE_POLICY_ABANDON)
                        .build());

        childPromise = Async.function(child::start,
                workflowInputChild);

        Promise<WorkflowExecution> childExecution = Workflow.getWorkflowExecution(child);

        // Wait for child to start
        childExecution.get();

        Async.procedure(this::get);

        return asyncChildResultCompletablePromise;
    }


    public AsyncChildResult get(){

        try {

            final AsyncChildResult value = new AsyncChildResult(id, childPromise.get());
            asyncChildResultCompletablePromise.complete(value);
            return value;


        }catch (Exception e){

            final RuntimeException failure = childPromise.getFailure();
            asyncChildResultCompletablePromise.completeExceptionally(new AsyncChildException(id, failure));
            throw failure;

        }
    }


}
