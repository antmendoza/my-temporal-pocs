package io.temporal.samples;

import io.grpc.*;
import io.grpc.ForwardingClientCall.SimpleForwardingClientCall;
import io.grpc.ForwardingClientCallListener.SimpleForwardingClientCallListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** gRPC ClientInterceptor that logs Temporal RespondWorkflowTaskCompleted calls. */
public class LoggingInterceptor implements ClientInterceptor {

  private static final Logger log = LoggerFactory.getLogger(LoggingInterceptor.class);

  private static final String FULL_METHOD_NAME =
      "temporal.api.workflowservice.v1.WorkflowService/RespondWorkflowTaskCompleted";

  @Override
  public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(
      MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
    ClientCall<ReqT, RespT> delegate = next.newCall(method, callOptions);

    String respondWorkflowTaskCompleted = method.getFullMethodName();

    if (
    //    FULL_METHOD_NAME.equals(method.getFullMethodName()) ||
    //    method.getFullMethodName().contains("Query")

    true) {

      return new SimpleForwardingClientCall<ReqT, RespT>(delegate) {
        private long startNanos;

        @Override
        public void start(Listener<RespT> responseListener, Metadata headers) {
          startNanos = System.nanoTime();
          log.info(respondWorkflowTaskCompleted + ": call started");

          super.start(
              new SimpleForwardingClientCallListener<RespT>(responseListener) {
                @Override
                public void onHeaders(Metadata headers) {
                  log.info(respondWorkflowTaskCompleted + ": headers received: {}", headers);
                  super.onHeaders(headers);
                }

                @Override
                public void onClose(Status status, Metadata trailers) {
                  long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
                  if (status.isOk()) {
                    log.info(respondWorkflowTaskCompleted + ": completed OK in {} ms", durationMs);
                  } else {
                    log.warn(
                        respondWorkflowTaskCompleted
                            + ": completed with status={} in {} ms; trailers={}",
                        status,
                        durationMs,
                        trailers);
                  }
                  super.onClose(status, trailers);
                }
              },
              headers);
        }

        @Override
        public void sendMessage(ReqT message) {
          // Protobuf messages have informative toString(); avoid logging huge payloads in prod.
          log.info(respondWorkflowTaskCompleted + ": request={}", message);
          super.sendMessage(message);
        }
      };
    }
    return delegate;
  }
}
