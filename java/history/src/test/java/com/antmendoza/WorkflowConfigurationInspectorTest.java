package com.antmendoza;


import com.antmendoza.loader.HistoryLoaderFromFile;
import com.antmendoza.loader.WorkflowExecutionHistoryData;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;


public class WorkflowConfigurationInspectorTest {

    @Test
    public void loadActivities() {

        final Path path = Path.of("src/test/resources", "4eb9c3ba-a113-4b12-b21c-63dd450671c2.json");
        final ConfigurationInspectorResult result = new WorkflowConfigurationInspector
                (new WorkflowExecutionHistoryData(new HistoryLoaderFromFile(path)
                        .read()))
                .feedback();


        assertEquals(1, result.getTips().size());

        assertEquals(new Tip(
                ConfigurationProperty.ActivityStartToClose,
                ActionSuggested.ReduceValue,
                Duration.ofMinutes(2),
                Duration.ofMillis(3)), result.getTips().get(0));


    }


}