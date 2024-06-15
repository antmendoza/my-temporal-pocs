package com.antmendoza.loader;

import io.temporal.common.WorkflowExecutionHistory;

import java.util.List;

public interface HistoryLoader {
    List<WorkflowExecutionHistory> read();
}
