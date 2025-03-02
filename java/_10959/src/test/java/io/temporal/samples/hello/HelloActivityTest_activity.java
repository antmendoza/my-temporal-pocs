package io.temporal.samples.hello;

import io.temporal.client.WorkflowClientOptions;
import io.temporal.testing.TestActivityEnvironment;
import io.temporal.testing.TestEnvironmentOptions;
import org.junit.Assert;
import org.junit.Test;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import static io.temporal.samples.hello.HelloActivityRunner.dataConverter;
import static org.junit.Assert.assertEquals;

public class HelloActivityTest_activity {



    TestActivityEnvironment testEnvironment = TestActivityEnvironment.newInstance(
            TestEnvironmentOptions.newBuilder()
                    .setWorkflowClientOptions(WorkflowClientOptions.newBuilder()
                            .setDataConverter(dataConverter).build())

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



        final AtomicReference<MyRequest2> result = new AtomicReference<>();
        CompletableFuture.runAsync(
                () -> {
                    TestActivity2<MyRequest2, MyRequest2> activity = testEnvironment.newActivityStub(TestActivity2.class);
                    MyRequest2 myRequest2 = activity.activity1(new MyRequest2("input1", "type1"));
                    result.set(myRequest2);
                });

        completableFuture.get();

        Assert.assertEquals("input1", result.get().getName());
    }


}
