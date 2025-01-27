﻿namespace TemporalioSamples.OpenTelemetry.Common;

using Microsoft.Extensions.Logging;
using Temporalio.Workflows;

[Workflow]
public class MyWorkflow
{
    [WorkflowRun]
    public async Task<string> RunAsync()
    {
        Workflow.Logger.LogInformation("Running workflow {WorkflowId}.", Workflow.Info.WorkflowId);

        Workflow.MetricMeter.CreateCounter<int>("my-workflow-counter", description: "Replay-safe counter for instrumentation inside a workflow.").Add(123);

        // await ExecuteActivityAsync();
        await Workflow.DelayAsync(TimeSpan.FromSeconds(10));

        await ExecuteActivityAsync();
        return "complete!";
    }

    private static Task ExecuteActivityAsync()
    {
        return Workflow.ExecuteActivityAsync(
            () => Activities.MyActivity("input"),
            new()
            {
                StartToCloseTimeout = TimeSpan.FromMinutes(5),
            });
    }
}