package com.antmendoza;

import io.temporal.common.WorkflowExecutionHistory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class HistoryLoaderFromDir implements HistoryLoader {

    private final Path path;

    public HistoryLoaderFromDir(final Path path) {
        this.path = path;
    }

    @Override
    public List<WorkflowExecutionHistory> read() {

        return loadFiles().stream().map(f -> {
            final Path filePath = Path.of(path.toString(), f);
            return new HistoryLoaderFromFile(filePath).read();
        }).collect(Collectors.toList());

    }

    private Collection<String> loadFiles() {
        try (Stream<Path> stream = Files.list(path)) {
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
