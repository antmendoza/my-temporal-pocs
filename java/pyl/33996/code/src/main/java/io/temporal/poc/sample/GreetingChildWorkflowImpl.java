package io.temporal.poc.sample;

public class GreetingChildWorkflowImpl implements GreetingChildWorkflow {

    @Override
    public String emphasize(String greeting) {
        return greeting + "!";
    }
}