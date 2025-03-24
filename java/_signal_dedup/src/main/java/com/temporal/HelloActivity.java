/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.temporal;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.*;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Sample Temporal Workflow Definition that executes a single Activity. */
public class HelloActivity {

  // Define the task queue name
  static final String TASK_QUEUE = "HelloActivityTaskQueue";

  // Define our workflow unique id
  static final String WORKFLOW_ID = "HelloActivityWorkflow";

  public static void main(String[] args) {

    WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();
    WorkflowClient client = WorkflowClient.newInstance(service);

    WorkerFactory factory = WorkerFactory.newInstance(client);

    Worker worker = factory.newWorker(TASK_QUEUE);


    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);


    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());


    factory.start();

    // Create the workflow client stub. It is used to start our workflow execution.
    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .build());


    String greeting = workflow.getGreeting("World");

    // Display workflow execution results
    System.out.println(greeting);
    System.exit(0);
  }


  @WorkflowInterface
  public interface GreetingWorkflow {


    @WorkflowMethod
    String getGreeting(String name);

    @SignalMethod
    void mySignal(String key, String value);
  }


  @ActivityInterface
  public interface GreetingActivities {

    // Define your activity method which can be called during workflow execution
    @ActivityMethod(name = "greet")
    String composeGreeting(String greeting, String name);
  }

  // Define the workflow implementation which implements our getGreeting workflow method.
  public static class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    Map<String, Promise<Void>> timers = new HashMap<>();
    private SignalContainer signalContainer = new SignalContainer();

    @Override
    public String getGreeting(String name) {

      while (true) {

        Workflow.await(() -> signalContainer.hasPendingSignals());

        String key = signalContainer.getNext();

        if (!timers.containsKey(key)) {

          Promise<Void> p =
              Workflow.newTimer(Duration.ofSeconds(5))
                  .thenApply(
                      result -> {

                        // Process deduplicated key
                        String myValue = signalContainer.markAsProcessed(key);

                        System.out.println(key +" : " + myValue);

                        timers.remove(key);

                        System.out.println(timers);

                        return null;
                      });

          timers.put(key, p);
        }
      }

      // This is a blocking call that returns only after the activity has completed.
      // return activities.composeGreeting("Hello", name);

    }

    @Override
    public void mySignal(final String key, final String value) {
      this.signalContainer.add(key, value);
    }

    private class SignalContainer {

      private Map<String, String> signals = new HashMap<>();
      private Map<String, String> pendingSignals = new HashMap<>();

      public void add(final String key, final String value) {
        this.signals.put(key, value);
        this.pendingSignals.put(key, value);
      }

      public boolean hasPendingSignals() {
        return !pendingSignals.isEmpty();
      }

      public String getNext() {
        final String next = pendingSignals.keySet().iterator().next();
        pendingSignals.remove(next);
        return next;
      }

      public String markAsProcessed(final String key) {
        return signals.remove(key);
      }
    }
  }

  /** Simple activity implementation, that concatenates two strings. */
  public static class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String composeGreeting(String greeting, String name) {
      log.info("Composing greeting...");
      return greeting + " " + name + "!";
    }
  }
}
