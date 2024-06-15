package com.antmendoza;

import io.temporal.common.WorkflowExecutionHistory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryLoader {

        private final Path path;

    public HistoryLoader(final Path path) {
        this.path = path;
    }

    public List<WorkflowExecutionHistory> read() {

        return  loadFiles().stream().map(f -> {
            try {
                final Path of = Path.of(path.toString(), f);
                String json = Files.readString(of);
            return WorkflowExecutionHistory.fromJson(json);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }).collect(Collectors.toList());

    }

    private Collection<String> loadFiles() {
        try (Stream<Path> stream = Files.list(path) ) {
            return stream
                    .filter(file -> !Files.isDirectory(file))
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .collect(Collectors.toSet());


        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
