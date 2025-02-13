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

package io.temporal.samples.hello;

import static org.junit.Assert.assertEquals;

import io.temporal.activity.Activity;
import io.temporal.activity.ActivityInterface;
import io.temporal.testing.TestActivityEnvironment;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.Test;

public class HelloActivityTest_activity {

  @ActivityInterface
  public interface TestActivity {
    String activity1(String input);
  }

  private static class ActivityImpl implements TestActivity {
    @Override
    public String activity1(String input) {
      final String hello = Activity.getExecutionContext().getInfo().getActivityType() + "-" + input;

      for (int i = 0; i < 2; i++) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e) {
          throw new RuntimeException(e);
        }

        System.out.println(hello);

        String heartbeat = i + 1 + "";
        Activity.getExecutionContext().heartbeat(heartbeat);
      }
      return "done";
    }
  }

  @Test
  public void testSuccess() throws ExecutionException, InterruptedException {

    TestActivityEnvironment testEnvironment = TestActivityEnvironment.newInstance();

    testEnvironment.registerActivitiesImplementations(new ActivityImpl());

    CompletableFuture<String> completableFuture = new CompletableFuture<String>();
    testEnvironment.setActivityHeartbeatListener(
        String.class,
        (info) -> {
          System.out.println("Heartbeat" + info);
          if (info.equals("2")) {
            completableFuture.complete("done");
          }
        });

    final AtomicReference<String> result = new AtomicReference<String>();
    CompletableFuture.runAsync(
        () -> {
          TestActivity activity = testEnvironment.newActivityStub(TestActivity.class);
          result.set(activity.activity1("input1"));
        });


    completableFuture.get();

    assertEquals("done", result.get());
  }
}
