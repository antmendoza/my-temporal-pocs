package com.antmendoza.temporal;

import com.antmendoza.temporal.config.ScopeBuilder;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;

import java.util.Map;


public class WorkerSsl_local_2 {


    public static void main(String[] args) throws Exception {

        
        final int port = 8073;
        Scope metricsScope = new ScopeBuilder().create(port, ImmutableMap.of(
                "worker",
                "WorkerSsl_" + port)
        );

        metricsScope.tagged(Map.of("my_worker_id", port + "_")).gauge("my_metric_name").update(1);


    }

}
