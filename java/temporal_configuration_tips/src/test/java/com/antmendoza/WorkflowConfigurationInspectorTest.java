package com.antmendoza;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.antmendoza.inspector.*;
import com.antmendoza.loader.HistoryLoaderFromFile;
import com.antmendoza.loader.WorkflowExecutionHistoryData;
import java.nio.file.Path;
import java.time.Duration;
import org.junit.jupiter.api.Test;

public class WorkflowConfigurationInspectorTest {

  @Test
  public void loadActivities() {

    final Path path = Path.of("src/test/resources", "4eb9c3ba-a113-4b12-b21c-63dd450671c2.json");
    final ConfigurationInspectorResult result =
        new WorkflowConfigurationInspector(
                new WorkflowExecutionHistoryData(new HistoryLoaderFromFile(path).read()))
            .feedback();

    assertEquals(1, result.getTips().size());

    assertEquals(
        new Tip(
            "ActivityData{workflowId='workflow_id_in_replay', activityId='a490efe7-fd1f-38fc-a914-7caa325a2422'}",
            Tip.ConfigurationProperty.ActivityStartToClose,
            "activityStartToClose configured valued is too high."
                + " Set the value to the maximum time the activity execution can take",
            Duration.ofMinutes(2),
            Duration.ofMillis(3)),
        result.getTips().get(0));
  }
}
