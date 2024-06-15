package com.antmendoza.loader;

import com.antmendoza.loader.HistoryLoaderFromFile;
import com.antmendoza.loader.WorkflowExecutionHistoryData;
import io.temporal.common.WorkflowExecutionHistory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.Assert.assertEquals;

public class WorkflowExecutionHistoryDataTest {


    @Test
    public void loadActivities() {

        final Path path = Path.of("src/test/resources", "4eb9c3ba-a113-4b12-b21c-63dd450671c2.json");
        final WorkflowExecutionHistory workflowExecutionHistory = new HistoryLoaderFromFile(path).read();
        assertEquals(1,new WorkflowExecutionHistoryData(workflowExecutionHistory).getActivityDataList().size());
    }

}