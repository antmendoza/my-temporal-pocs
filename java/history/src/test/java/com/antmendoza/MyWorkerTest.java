package com.antmendoza;

import io.temporal.common.WorkflowExecutionHistory;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;
import java.util.List;

public class MyWorkerTest {


    @Test
    public void loadHistories() {

        final Path path = Path.of("src/test/resources", "");


        final List<WorkflowExecutionHistory> list = new MyHistoryLoader(path).read();


        System.out.println("l " +list.size() );



    }
}