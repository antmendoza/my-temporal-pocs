package io.temporal.samples.hello;

import com.google.protobuf.ByteString;
import io.temporal.activity.ActivityInterface;
import io.temporal.activity.ActivityMethod;
import io.temporal.activity.ActivityOptions;
import io.temporal.api.common.v1.Payload;
import io.temporal.client.WorkflowClient;
import io.temporal.client.WorkflowOptions;
import io.temporal.common.SimplePlugin;
import io.temporal.common.converter.CodecDataConverter;
import io.temporal.envconfig.ClientConfigProfile;
import io.temporal.payload.codec.PayloadCodec;
import io.temporal.payload.codec.PayloadCodecException;
import io.temporal.serviceclient.WorkflowServiceStubs;
import io.temporal.worker.Worker;
import io.temporal.worker.WorkerFactory;
import io.temporal.workflow.Workflow;
import io.temporal.workflow.WorkflowInterface;
import io.temporal.workflow.WorkflowMethod;
import java.io.IOException;
import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Demonstrates using a Temporal {@link SimplePlugin} with a custom data converter on the
 * HelloActivity example.
 *
 * <p>The plugin wraps the active {@link io.temporal.common.converter.DataConverter} with a {@link
 * CodecDataConverter} that layers a custom {@link PayloadCodec} on top of it. The codec here adds a
 * simple byte-prefix to every payload, which shows the intercept/transform pattern that real-world
 * plugins use for encryption or compression.
 *
 * <p>Because the plugin is registered on the {@link
 * io.temporal.client.WorkflowClientOptions.Builder}, both the {@link WorkflowClient} and the {@link
 * Worker} created from the same client automatically share the same data converter — no per-worker
 * wiring is required.
 */
public class HelloActivityPlugin {

  // SNIP_START HelloActivityPlugin_taskqueue
  static final String TASK_QUEUE = "HelloActivityPluginTaskQueue";
  // SNIP_END HelloActivityPlugin_taskqueue

  static final String WORKFLOW_ID = "HelloActivityPluginWorkflow";

  @WorkflowInterface
  public interface GreetingWorkflow {

    @WorkflowMethod
    String getGreeting(String name);
  }

  @ActivityInterface
  public interface GreetingActivities {

    @ActivityMethod(name = "greet")
    String composeGreeting(String greeting, String name);
  }

  public static class GreetingWorkflowImpl implements GreetingWorkflow {

    private final GreetingActivities activities =
        Workflow.newActivityStub(
            GreetingActivities.class,
            ActivityOptions.newBuilder().setStartToCloseTimeout(Duration.ofSeconds(2)).build());

    @Override
    public String getGreeting(String name) {
      return activities.composeGreeting("Hello", name);
    }
  }

  public static class GreetingActivitiesImpl implements GreetingActivities {
    private static final Logger log = LoggerFactory.getLogger(GreetingActivitiesImpl.class);

    @Override
    public String composeGreeting(String greeting, String name) {
      log.info("Composing greeting...");
      return greeting + " " + name + "!";
    }
  }

  // SNIP_START HelloActivityPlugin_codec
  /**
   * A {@link PayloadCodec} that adds a fixed text prefix to every encoded payload's raw data.
   *
   * <p>This is intentionally simple — a production codec would encrypt or compress the bytes
   * instead. The encode/decode pair must be inverses of each other so that payloads round-trip
   * correctly.
   */
  static class PrefixPayloadCodec implements PayloadCodec {

    static final ByteString PREFIX = ByteString.copyFromUtf8("greeting-plugin: ");

    @NotNull
    @Override
    public List<Payload> encode(@NotNull List<Payload> payloads) {
      return payloads.stream().map(this::addPrefix).collect(Collectors.toList());
    }

    @NotNull
    @Override
    public List<Payload> decode(@NotNull List<Payload> payloads) {
      return payloads.stream().map(this::removePrefix).collect(Collectors.toList());
    }

    private Payload addPrefix(Payload payload) {
      return payload.toBuilder().setData(PREFIX.concat(payload.getData())).build();
    }

    private Payload removePrefix(Payload payload) {
      ByteString data = payload.getData();
      if (!data.startsWith(PREFIX)) {
        throw new PayloadCodecException("Payload is missing the expected plugin prefix");
      }
      return payload.toBuilder().setData(data.substring(PREFIX.size())).build();
    }
  }

  // SNIP_END HelloActivityPlugin_codec

  // SNIP_START HelloActivityPlugin_build
  /**
   * Builds a {@link SimplePlugin} that customizes the data converter.
   *
   * <p>{@link SimplePlugin#newBuilder} accepts a unique plugin name (reverse-DNS convention).
   * {@link io.temporal.common.SimplePlugin.Builder#customizeDataConverter} receives the existing
   * {@link io.temporal.common.converter.DataConverter} configured on the client and must return the
   * replacement. Here we wrap it inside a {@link CodecDataConverter} that chains our {@link
   * PrefixPayloadCodec}.
   */
  static SimplePlugin buildGreetingPlugin() {
    return SimplePlugin.newBuilder("io.temporal.samples.hello.GreetingPlugin")
        .customizeDataConverter(
            existing ->
                new CodecDataConverter(
                    existing, Collections.singletonList(new PrefixPayloadCodec())))
        .build();
  }

  // SNIP_END HelloActivityPlugin_build

  public static void main(String[] args) {

    ClientConfigProfile profile;
    try {
      profile = ClientConfigProfile.load();
    } catch (IOException e) {
      throw new RuntimeException("Failed to load client configuration", e);
    }

    WorkflowServiceStubs service =
        WorkflowServiceStubs.newServiceStubs(profile.toWorkflowServiceStubsOptions());

    // SNIP_START HelloActivityPlugin_register
    // Build the plugin once and pass it to the WorkflowClientOptions.
    // The SDK propagates the plugin's data converter to every Worker created from this client,
    // so there is no need to configure the codec on the WorkerFactory separately.
    SimplePlugin greetingPlugin = buildGreetingPlugin();

    WorkflowClient client =
        WorkflowClient.newInstance(
            service,
            profile.toWorkflowClientOptions().toBuilder()
                    .setPlugins(greetingPlugin).build());
    // SNIP_END HelloActivityPlugin_register

    WorkerFactory factory = WorkerFactory.newInstance(client);
    Worker worker = factory.newWorker(TASK_QUEUE);
    worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl.class);
    worker.registerActivitiesImplementations(new GreetingActivitiesImpl());
    factory.start();

    GreetingWorkflow workflow =
        client.newWorkflowStub(
            GreetingWorkflow.class,
            WorkflowOptions.newBuilder()
                .setWorkflowId(WORKFLOW_ID)
                .setTaskQueue(TASK_QUEUE)
                .build());

    String greeting = workflow.getGreeting("World");
    System.out.println(greeting);
    System.exit(0);
  }
}
