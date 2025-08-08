package io.temporal.samples.earlyreturn;

import io.grpc.*;
import io.temporal.api.workflowservice.v1.PollWorkflowTaskQueueResponse;
import io.temporal.api.workflowservice.v1.RespondWorkflowTaskCompletedRequest;
import java.time.Instant;

public class GrpcLoggingInterceptor implements ClientInterceptor {

  // public static for the sake of simplicity for this example
  public Instant firstPollWorkflowTaskQueueResponse = null;
  public Instant firstRespondWorkflowTaskCompletedRequest = null;

  public void resetFirstWorkflowTaskRecords() {
    firstPollWorkflowTaskQueueResponse = null;
    firstRespondWorkflowTaskCompletedRequest = null;
  }

  public Instant getTimeFirstWorkflowTaskExecution() {
    return firstRespondWorkflowTaskCompletedRequest.minusMillis(
        firstPollWorkflowTaskQueueResponse.toEpochMilli());
  }

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {

    return new ForwardingClientCall.SimpleForwardingClientCall<>(
        next.newCall(method, callOptions)) {

      @Override
      public void sendMessage(ReqT message) {

        if (message.getClass() == RespondWorkflowTaskCompletedRequest.class) {
          if (firstRespondWorkflowTaskCompletedRequest == null) {
            firstRespondWorkflowTaskCompletedRequest = Instant.now();
          }
        }
        super.sendMessage(message);
      }

      @Override
      public void start(Listener<RespT> responseListener, Metadata headers) {
        super.start(
            new ForwardingClientCallListener.SimpleForwardingClientCallListener<>(
                responseListener) {

              @Override
              public void onMessage(RespT message) {

                if (message.getClass() == PollWorkflowTaskQueueResponse.class) {
                  if (firstPollWorkflowTaskQueueResponse == null) {
                    firstPollWorkflowTaskQueueResponse = Instant.now();
                  }
                }

                super.onMessage(message);
              }
            },
            headers);
      }
    };
  }
}
