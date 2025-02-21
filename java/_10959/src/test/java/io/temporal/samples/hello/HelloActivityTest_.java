package io.temporal.samples.hello;

import io.temporal.activity.ActivityInterface;
import io.temporal.testing.TestActivityEnvironment;
import io.temporal.testing.TestEnvironmentOptions;
import mypackage.SimpleTask;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;
import org.junit.Test;




public class HelloActivityTest_ {


    TestActivityEnvironment testEnvironment = TestActivityEnvironment.newInstance(
            TestEnvironmentOptions
                    .newBuilder()
                    .build()
    );

    @Test(timeout = 10_000)
    public void testSuccess()  {

        testEnvironment.registerActivitiesImplementations(new ActivityImpl_());


        TestActivity_ activity = testEnvironment.newActivityStub(TestActivity_.class);
        String result = activity.activity_never_returns("input1");

        Assert.assertEquals("input1", result);


    }


    @ActivityInterface
    public interface TestActivity_ {
        String activity_never_returns(String input);
    }

    public static class ActivityImpl_ implements TestActivity_ {

        @Override
        public String activity_never_returns(final String input) {

            while(true){
                try {
                    Thread.sleep(1_000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

        }
    }

}
