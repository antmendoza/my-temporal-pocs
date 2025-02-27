package io.temporal.samples.hello.new_;

import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityOptions;
import io.temporal.activity.DynamicActivity;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.DefaultDataConverter;
import io.temporal.common.converter.EncodedValues;
import io.temporal.samples.hello.MyPayloadConverter;
import io.temporal.testing.*;
import io.temporal.workflow.ActivityStub;
import io.temporal.workflow.Workflow;
import org.junit.Rule;
import org.junit.Test;
import org.junit.jupiter.api.extension.RegisterExtension;

import java.time.Duration;

import static org.junit.Assert.assertEquals;


public class HelloDynamicActivityTest {


    @Rule
    public TestWorkflowRule testWorkflowRule =
            TestWorkflowRule.newBuilder()
                    .setWorkflowTypes(WorkflowsImpl.class)
                    .setDoNotStart(true)
                    .build();



    public static final DefaultDataConverter dataConverter =
            new DefaultDataConverter().withPayloadConverterOverrides(new MyPayloadConverter());


    @Test
    public void testMyDynamicActivityString() {
        final TestActivityEnvironment testActivityEnvironment = getTestActivityEnvironment();
        testActivityEnvironment.registerActivitiesImplementations(new MyDynamicActivityString());
        DynamicActivity activity = testActivityEnvironment.newActivityStub(MyStaticActivity.class);
        Object result = activity.execute(new EncodedValues(new Object[]{"anything"}));
        assertEquals("done", result);
    }


    @Test
    public void testMyDynamicActivityComplexType() {
        final TestActivityEnvironment testActivityEnvironment = getTestActivityEnvironment();
        testActivityEnvironment.registerActivitiesImplementations(new MyDynamicActivityComplexType());
        DynamicActivity activity = testActivityEnvironment.newActivityStub(MyStaticActivity.class);
        Object result = activity.execute(new EncodedValues(new Object[]{"anything"}));
        assertEquals(new ComplexType("name", 1), result);
    }


    @ActivityInterface
    public interface MyStaticActivity<T> extends DynamicActivity {
        T execute(EncodedValues args);
    }


//*****//

    @Test
    public void testMyWorkflowComplexType() {
        final TestWorkflowEnvironment testEnv = testWorkflowRule.getTestEnvironment();

        final ComplexType returned = new ComplexType("name", 1);

        testWorkflowRule.getWorker().registerActivitiesImplementations(new MyDynamicActivityComplexType(returned));
        System.out.println("testEnv: " + testWorkflowRule.getTaskQueue());
        testEnv.start();
        
        MyWorkflow<ComplexType> workflow =
                testWorkflowRule
                        .getWorkflowClient()
                        .newWorkflowStub(
                                MyWorkflow.class,
                                WorkflowOptions.newBuilder().setTaskQueue(testWorkflowRule.getTaskQueue()).build());


        final ComplexType result = workflow.getGreeting("name");

        assertEquals(new ComplexType("name", 1), result);
    }





    private TestActivityEnvironment getTestActivityEnvironment() {
        WorkflowClientOptions wo = WorkflowClientOptions.newBuilder()
                .setDataConverter(dataConverter)

                .build();
        TestEnvironmentOptions options = TestEnvironmentOptions.newBuilder()
                .setWorkflowClientOptions(wo)
                .build();
        return TestActivityEnvironment.newInstance(options);

    }


    public static class WorkflowsImpl<T> implements MyWorkflow {


        @Override
        public T getGreeting(final String name) {

            ActivityStub activity =
                    Workflow.newUntypedActivityStub(
                            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(10)).build());

            activity.execute("DynamicACT", Object.class, null);

            return (T) "greetingResult";

        }
    }



}

