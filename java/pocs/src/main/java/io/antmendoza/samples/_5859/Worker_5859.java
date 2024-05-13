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

package io.antmendoza.samples._5859;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowClientOptions;
import io.temporal.common.converter.*;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;

import java.util.Collections;

import static io.antmendoza.samples._5859.Client_5859.TASK_QUEUE;

public class Worker_5859 {



    public static void main(String[] args) {
        WorkflowServiceStubs service = WorkflowServiceStubs.newLocalServiceStubs();

        startWorker(service);


    }

    private static void startWorker(final WorkflowServiceStubs service) {


        CodecDataConverter codecDataConverter =
                new CodecDataConverter(
                        // For sample we just use default data converter
                        DefaultDataConverter.newDefaultInstance(),
                        // Simple prefix codec to encode/decode
                        Collections.singletonList(new SimplePrefixPayloadCodec()),
                        true); // Setting encodeFailureAttributes to true

        // WorkflowClient uses our CodecDataConverter
        WorkflowClient client =
                WorkflowClient.newInstance(
                        service,
                        WorkflowClientOptions.newBuilder()
                                //.setDataConverter(DefaultDataConverter.newDefaultInstance()
                                //        .withPayloadConverterOverrides(test()))

                                .setDataConverter(codecDataConverter)
                                .build());


        WorkerFactory factory = WorkerFactory.newInstance(client);

        Worker worker = factory.newWorker(TASK_QUEUE);

        worker.registerWorkflowImplementationTypes(GreetingWorkflow_5859.GreetingWorkflowImpl.class);

        worker.registerActivitiesImplementations(new GreetingWorkflow_5859.GreetingActivitiesImpl());

        factory.start();
    }

    private static PayloadConverter test() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new GuavaModule()); // I saw issues with this for our custom type
        //objectMapper.registerModule(new Jdk8Module());
        //objectMapper.registerModule(new JavaTimeModule());
        // Add your custom logic here.
        return new JacksonJsonPayloadConverter(objectMapper);
    }


}
