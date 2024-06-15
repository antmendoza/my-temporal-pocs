package com.antmendoza.loader;

import com.antmendoza.loader.HistoryLoaderFromDir;
import io.temporal.common.WorkflowExecutionHistory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;
import static org.junit.Assert.*;


public class HistoryLoaderTest {


    @Test
    public void loadHistories() {
        final Path path = Path.of("src/test/resources", "");
        final List<WorkflowExecutionHistory> list = new HistoryLoaderFromDir(path).read();
        assertEquals(10,list.size());
    }
}