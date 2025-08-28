using Temporalio.Workflows;

namespace TemporalioSamples.Polling.PeriodicSequence;

[Workflow]
public class PeriodicPollingChildWorkflow
{
    [WorkflowRun]
    public async Task<string> RunAsync()
    {
        await Workflow.ExecuteLocalActivityAsync(
            (MyActivities a) => a.DelayAsync(0),
            new()
            {
                StartToCloseTimeout = TimeSpan.FromSeconds(5),
                RetryPolicy = new()
                {
                    MaximumAttempts = 1,
                },
            });

        return "OK";
    }
}