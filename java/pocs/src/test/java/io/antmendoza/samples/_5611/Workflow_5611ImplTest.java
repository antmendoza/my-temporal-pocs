package io.antmendoza.samples._5611;

import io.temporal.testing.TestWorkflowExtension;
import junit.framework.TestCase;
import kotlin.jvm.JvmField;
import org.junit.jupiter.api.extension.RegisterExtension;

public class Workflow_5611ImplTest  {


    @JvmField
    @RegisterExtension
    val testWorkflowExtension: TestWorkflowExtension =
            TestWorkflowExtension.newBuilder()
            .setWorkflowTypes(
            TargetValidationWorkflowImpl::class.java,
            )
      .setActivityImplementations(validationActivities, validationMonitoringActivities, campaignActivities)
      .setWorkflowClientOptions(workflowClientOptions)
      .setUseTimeskipping(true)
      .build()

}