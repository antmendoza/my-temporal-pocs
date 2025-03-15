/*
 *  Copyright (c) 2020 Temporal Technologies, Inc. All Rights Reserved
 *
 *  Copyright 2012-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 *  Modifications copyright (C) 2017 Uber Technologies, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"). You may not
 *  use this file except in compliance with the License. A copy of the License is
 *  located at
 *
 *  http://aws.amazon.com/apache2.0
 *
 *  or in the "license" file accompanying this file. This file is distributed on
 *  an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 *  express or implied. See the License for the specific language governing
 *  permissions and limitations under the License.
 */

package com.antmendoza.temporal;

import com.antmendoza.temporal.config.ScopeBuilder;
import com.antmendoza.temporal.config.SslContextBuilderProvider;
import com.antmendoza.temporal.workflow.MyWorkflow1;
import com.uber.m3.tally.Scope;
import com.uber.m3.util.ImmutableMap;
import io.temporal.api.common.v1.Payload;
import io.temporal.api.common.v1.Payloads;
import io.temporal.api.common.v1.WorkflowExecution;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.converter.DataConverter;
import io.temporal.common.converter.DataConverterException;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.serviceclient.WorkflowServiceStubsOptions;
import io.temporal.workflow.unsafe.WorkflowUnsafe;

import java.lang.reflect.Type;
import java.util.Date;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;


public class Starter {


    public static final String TASK_QUEUE = "MyTaskQueue_";


    public static void main(String[] args) throws InterruptedException {

        SslContextBuilderProvider sslContextBuilderProvider = new SslContextBuilderProvider();


        final int port = 8079;
        Scope metricsScope = new ScopeBuilder().create(port, ImmutableMap.of(
                "client",
                "ClientSsl_" + port)
        );

        final WorkflowServiceStubsOptions.Builder builder = WorkflowServiceStubsOptions.newBuilder()
                .setMetricsScope(metricsScope)
//                                .setRpcTimeout(Duration.ofMillis(167))
                .setTarget(sslContextBuilderProvider.properties.getTemporalStarterTargetEndpoint());

        if(sslContextBuilderProvider.getSslContext() != null) {
            builder.setSslContext(sslContextBuilderProvider.getSslContext());
        }

        WorkflowServiceStubs service =
                WorkflowServiceStubs.newServiceStubs(
                        builder
                                .build());


        final DataConverter dataConverter = new DataConverter() {
            @Override
            public <T> Optional<Payload> toPayload(final T value) throws DataConverterException {
                return WorkflowUnsafe.deadlockDetectorOff(
                        () -> {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                            return Optional.empty();
                        });

            }

            @Override
            public <T> T fromPayload(final Payload payload, final Class<T> valueClass, final Type valueType) throws DataConverterException {
                return WorkflowUnsafe.deadlockDetectorOff(
                        () -> {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                            return null;
                        });
            }

            @Override
            public Optional<Payloads> toPayloads(final Object... values) throws DataConverterException {
                return WorkflowUnsafe.deadlockDetectorOff(
                        () -> {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                            return Optional.empty();
                        });
            }

            @Override
            public <T> T fromPayloads(final int index, final Optional<Payloads> content, final Class<T> valueType, final Type valueGenericType) throws DataConverterException {
                return WorkflowUnsafe.deadlockDetectorOff(
                        () -> {
                            try {
                                Thread.sleep(2000);
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                                throw new RuntimeException(e);
                            }
                            return null;
                        });
            }
        };
        WorkflowClientOptions clientOptions =
                WorkflowClientOptions.newBuilder()
                        .setNamespace(sslContextBuilderProvider.properties.getTemporalNamespace())
//                        .setDataConverter(dataConverter)
                        .build();
        WorkflowClient client = WorkflowClient.newInstance(service, clientOptions);


        final int millisSleep = 1000;

        final AtomicInteger input = new AtomicInteger();
        //while (input.get() < 1000) {


            final int andIncrement = input.getAndIncrement();
            CompletableFuture.runAsync(() -> {
                final String workflowId = andIncrement + "-" + Math.random();
                try {

                    WorkflowOptions workflowOptions =
                            WorkflowOptions.newBuilder()
                                    .setTaskQueue(TASK_QUEUE)
                                    .setWorkflowId(workflowId)
                                    .build();


                    MyWorkflow1 workflow = client.newWorkflowStub(MyWorkflow1.class, workflowOptions);
                    System.out.println("Starting workflow...with after = " + millisSleep + " ms");
                    System.out.println(workflowId + "init " + new Date());
                    WorkflowExecution execution = WorkflowClient.start(workflow::run, "" + andIncrement);
                    System.out.println(workflowId + "end " + new Date());


                } catch (Exception e) {

                    System.out.println("Failed workflowId = " + workflowId);
                }
            });

            Thread.sleep(millisSleep);


       // }




    }


}
