package com.antmendoza.temporal.config;


import com.google.protobuf.DoubleValue;
import io.grpc.*;
import io.temporal.api.workflowservice.v1.PollActivityTaskQueueRequest;

import java.time.Instant;

public class GrpcLoggingInterceptor implements ClientInterceptor {


    private DoubleValue LastMaxTasksPerSecond = DoubleValue.of(-1);

    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
            MethodDescriptor<ReqT, RespT> method,
            CallOptions callOptions,
            Channel next) {

        return new ForwardingClientCall.SimpleForwardingClientCall<>(next.newCall(method, callOptions)) {

            @Override
            public void sendMessage(ReqT message) {

                // Called when request payload is sent
                //System.out.printf("[%s]     Payload type: %s%n",
                //        Instant.now(),
                //        message != null ? message.getClass().getName() : "null");

                if (message.getClass() == PollActivityTaskQueueRequest.class) {
                    PollActivityTaskQueueRequest request = (PollActivityTaskQueueRequest) message;
                    DoubleValue getMaxTasksPerSecond = request.getTaskQueueMetadata().getMaxTasksPerSecond();

                    // Log only if the value has changed
                    if (
                            getMaxTasksPerSecond.getValue() != LastMaxTasksPerSecond.getValue()) {
                        System.out.printf("[%s]  MaxTasksPerSecond changed: %s -> %s%n",
                                Instant.now(),
                                LastMaxTasksPerSecond,
                                getMaxTasksPerSecond != null && getMaxTasksPerSecond.getValue() > 0 ? getMaxTasksPerSecond.getValue() : "----");
                        System.out.printf("[%s]  message: %s%n",
                                Instant.now(),
                                message);


                    }
                    LastMaxTasksPerSecond = getMaxTasksPerSecond;

                }
                // If you want to see the whole message, uncomment (careful, could be big):
                // System.out.println("Message: " + message);
                super.sendMessage(message);
            }

            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                // Called before the call starts, headers can be inspected here
                super.start(new ForwardingClientCallListener
                        .SimpleForwardingClientCallListener<>(responseListener) {

                    @Override
                    public void onMessage(RespT message) {
                        // Log responses from the server
                        // System.out.printf("[%s] <<< Response type: %s%n",
                        //        Instant.now(),
                        //        message != null ? message.getClass().getName() : "null");

                        super.onMessage(message);
                    }
                }, headers);
            }
        };
    }
}
