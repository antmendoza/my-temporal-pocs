package com.antmendoza;

import com.antmendoza.inspector.ConfigurationInspectorResult;
import com.antmendoza.inspector.WorkflowConfigurationInspector;
import com.antmendoza.loader.HistoryLoaderFromFile;
import com.antmendoza.loader.WorkflowExecutionHistoryData;
import java.nio.file.Path;

public class Main {

  public static void main(String[] args) {

    final Path path = Path.of("src/main/resources", "4eb9c3ba-a113-4b12-b21c-63dd450671c2.json");
    System.out.println(path.toAbsolutePath());

    final ConfigurationInspectorResult result =
        new WorkflowConfigurationInspector(
                new WorkflowExecutionHistoryData(new HistoryLoaderFromFile(path).read()))
            .feedback();

    System.out.println("Result " + result);
  }
}
