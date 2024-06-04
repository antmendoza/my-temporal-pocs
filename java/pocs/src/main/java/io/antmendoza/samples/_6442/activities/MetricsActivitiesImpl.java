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

package io.antmendoza.samples._6442.activities;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityExecutionContext;
import io.temporal.client.ActivityCompletionClient;

import java.util.concurrent.ForkJoinPool;

public class MetricsActivitiesImpl implements MetricsActivities {

  private final ActivityCompletionClient completionClient;

  public MetricsActivitiesImpl(final ActivityCompletionClient completionClient) {
    this.completionClient = completionClient;
  }

  @Override
  public String performSync(String input) {
    return "Performed activity A with input " + input + "\n";
  }

  @Override
  public String performAsync(String input) {
    return "Performed activity B with input " + input + "\n";
  }




  @Override
  public String completeWithCompletionClient(String greeting, String name) {

    // Get the activity execution context
    ActivityExecutionContext context = Activity.getExecutionContext();

    // Set a correlation token that can be used to complete the activity asynchronously
    byte[] taskToken = context.getTaskToken();

    /*
     * For the example we will use a {@link java.util.concurrent.ForkJoinPool} to execute our
     * activity. In real-life applications this could be any service. The composeGreetingAsync
     * method is the one that will actually complete workflow action execution.
     */
    ForkJoinPool.commonPool().execute(() -> composeGreetingAsync(taskToken, greeting, name));
    context.doNotCompleteOnReturn();

    // Since we have set doNotCompleteOnReturn(), the workflow action method return value is
    // ignored.
    return "ignored";
  }

  // Method that will complete action execution using the defined ActivityCompletionClient
  private void composeGreetingAsync(byte[] taskToken, String greeting, String name) {
    String result = greeting + " " + name + "!";

    try {
      Thread.sleep(10000);
    } catch (InterruptedException e) {
      throw new RuntimeException(e);
    }

    System.out.println("Completing");
    // Complete our workflow activity using ActivityCompletionClient
    completionClient.complete(taskToken, result);
  }
  private void incRetriesCustomMetric(ActivityExecutionContext context) {
    // We can create a child scope and add extra tags
    //    Scope scope =
    //        context
    //            .getMetricsScope()
    //            .tagged(
    //                Stream.of(
    //                        new String[][] {
    //                          {"workflow_id", context.getInfo().getWorkflowId()},
    //                          {"activity_id", context.getInfo().getActivityId()},
    //                          {
    //                            "activity_start_to_close_timeout",
    //                            context.getInfo().getStartToCloseTimeout().toString()
    //                          },
    //                        })
    //                    .collect(Collectors.toMap(data -> data[0], data -> data[1])));
    //
    //    scope.counter("custom_activity_retries").inc(1);

    // For sample we use root scope
    context.getMetricsScope().counter("custom_activity_retries").inc(1);
  }
}
