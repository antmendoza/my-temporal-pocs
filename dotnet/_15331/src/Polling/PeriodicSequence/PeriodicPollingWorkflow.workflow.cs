using Temporalio.Workflows;

namespace TemporalioSamples.Polling.PeriodicSequence;

[Workflow]
public class PeriodicPollingWorkflow
{
    [WorkflowRun]
    public async Task<string> RunAsync()
    {
        _ = await Workflow.StartChildWorkflowAsync(
            (PeriodicPollingChildWorkflow wf) => wf.RunAsync(),
            new ChildWorkflowOptions
            {
                ParentClosePolicy = ParentClosePolicy.Abandon,
            });

        // this is to introduce a small delay to allow the child workflow to complete
        await Workflow.ExecuteLocalActivityAsync(
            (MyActivities a) => a.DelayAsync(20),
            new()
            {
                StartToCloseTimeout = TimeSpan.FromSeconds(5),
                RetryPolicy = new()
                {
                    MaximumAttempts = 1,
                },
            });

        throw Workflow.CreateContinueAsNewException((PeriodicPollingWorkflow wf) => wf.RunAsync());
    }
}