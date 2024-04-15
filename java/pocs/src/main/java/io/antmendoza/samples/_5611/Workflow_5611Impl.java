package io.antmendoza.samples._5611;


import io.temporal.workflow.Workflow;

import java.time.Duration;

public class Workflow_5611Impl implements Workflow_5611 {


    @Override
    public void run() {

        Workflow.sleep(Duration.ofSeconds(5));


    }


    public static void main(String[] args) {
        System.out.println(">>>>>>> ");
    }
}
