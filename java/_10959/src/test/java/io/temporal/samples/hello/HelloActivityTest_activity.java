package io.temporal.samples.hello;

import io.temporal.testing.TestActivityEnvironment;
import io.temporal.testing.TestEnvironmentOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.Assert.assertEquals;

public class HelloActivityTest_activity {



    TestActivityEnvironment testEnvironment = TestActivityEnvironment.newInstance(
            TestEnvironmentOptions
                    .newBuilder()
                    .build()
    );

    @Test
    public void testSuccess() throws ExecutionException, InterruptedException {

        CompletableFuture<String> completableFuture = new CompletableFuture<String>();

        testEnvironment.registerActivitiesImplementations(new ActivityImpl());
        testEnvironment.setActivityHeartbeatListener(
                String.class,
                (info) -> {
                    System.out.println("Heartbeat" + info);
                    if (info.equals("2")) {
                        completableFuture.complete("done");
                    }
                });



        final AtomicReference<MyRequest> result = new AtomicReference<>();
        CompletableFuture.runAsync(
                () -> {
                    TestActivity activity = testEnvironment.newActivityStub(TestActivity.class);
                    activity.activity1(new MyRequest("input1", "type1"));
                    result.set(new MyRequest("input1", "type1"));
                });

        completableFuture.get();

        Assert.assertEquals("input1", result.get().getName());
    }


}
