using Temporalio.Activities;

namespace TemporalioSamples.Polling.PeriodicSequence;

public class MyActivities
{
    public MyActivities()
    {
    }

    [Activity]
    public async Task<string> DelayAsync(int delayMs)
    {
        await Task.Delay(delayMs);
        return "something";
    }
}