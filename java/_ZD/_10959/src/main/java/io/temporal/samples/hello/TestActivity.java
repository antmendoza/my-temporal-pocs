package io.temporal.samples.hello;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface TestActivity<K, T> {
    T activity1(K input);
}
