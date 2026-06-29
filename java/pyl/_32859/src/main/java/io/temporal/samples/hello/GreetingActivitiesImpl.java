package io.temporal.samples.hello;

public class GreetingActivitiesImpl implements GreetingActivities {
    @Override
    public String IsAdditionalEvidenceCollectionEnabled(int msSleep) {


        try {
            Thread.sleep(msSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "done";
    }

    @Override
    public String Method2(int msSleep) {
        try {
            Thread.sleep(msSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "done";    }

    @Override
    public String Method3(int msSleep) {
        try {
            Thread.sleep(msSleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return "done";    }
}
