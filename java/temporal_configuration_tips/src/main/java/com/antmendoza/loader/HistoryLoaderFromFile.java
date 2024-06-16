package com.antmendoza.loader;

import io.temporal.common.WorkflowExecutionHistory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class HistoryLoaderFromFile {
  private Path filePatch;

  public HistoryLoaderFromFile(final Path filePatch) {
    this.filePatch = filePatch;
  }

  public WorkflowExecutionHistory read() {

    String json = null;
    try {
      json = Files.readString(this.filePatch);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    return WorkflowExecutionHistory.fromJson(json);
  }
}
