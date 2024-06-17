package org.example.workflow;

import io.temporal.activity.ActivityInterface;

@ActivityInterface
public interface ReviewActivity {
    void submitReview(String title, String review);
}
