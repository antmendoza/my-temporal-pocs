package com.antmendoza.loader;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.temporal.common.WorkflowExecutionHistory;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;

public class HistoryLoaderTest {

  @Test
  public void loadHistories() {
    final Path path = Path.of("src/test/resources", "");
    final List<WorkflowExecutionHistory> list = new HistoryLoaderFromDir(path).read();
    assertEquals(10, list.size());
  }
}
