package io.temporal.samples.hello;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface GreetingActivities {
    String IsAdditionalEvidenceCollectionEnabled(int msSleep);



    String Method2(int msSleep);

    String Method3(int msSleep);
}
