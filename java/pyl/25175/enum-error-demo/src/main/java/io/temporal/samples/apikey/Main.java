package io.temporal.samples.apikey;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.KeyDeserializer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import io.temporal.api.common.v1.Payload;
import io.temporal.common.converter.GlobalDataConverter;
import java.util.Map;

public class Main {

  public static void main(String[] args) {
    System.out.println("=== Test 1: Using GlobalDataConverter (standard behavior) ===");
    executeWithGlobalConverter("{\"dagInfo\":{\"b2b_a_export\":\"some_value\"}}");
    executeWithGlobalConverter("{\"dagInfo\":{\"B2B_A_EXPORT\":\"some_value\"}}");

  //  System.out.println("\n=== Test 2: Using Custom KeyDeserializer (calls Enum.valueOf) ===");
  //  executeWithCustomDeserializer("{\"dagInfo\":{\"b2b_a_export\":\"some_value\"}}");
  //  executeWithCustomDeserializer("{\"dagInfo\":{\"B2B_A_EXPORT\":\"some_value\"}}");
  }

  private static void executeWithGlobalConverter(String json) {
    try {
      Payload payload =
          Payload.newBuilder()
              .putMetadata("encoding", com.google.protobuf.ByteString.copyFromUtf8("json/plain"))
              .setData(com.google.protobuf.ByteString.copyFromUtf8(json))
              .build();

      TemporalWorkflowContext context =
          GlobalDataConverter.get()
              .fromPayload(payload, TemporalWorkflowContext.class, TemporalWorkflowContext.class);

      System.out.println("Success: " + context);
    } catch (Exception e) {
      System.err.println("Error occurred: " + e.getMessage());
      printExceptionChain(e);
    }
  }

  private static void executeWithCustomDeserializer(String json) {
    try {
      // Create ObjectMapper with custom KeyDeserializer
      ObjectMapper mapper = new ObjectMapper();
      SimpleModule module = new SimpleModule();
      module.addKeyDeserializer(JobType.class, new JobTypeKeyDeserializer());
      mapper.registerModule(module);

      // Deserialize directly
      TemporalWorkflowContext context = mapper.readValue(json, TemporalWorkflowContext.class);

      System.out.println("Success: " + context);
    } catch (Exception e) {
      System.err.println("Error occurred: " + e.getMessage());
      printExceptionChain(e);
    }
  }

  private static void printExceptionChain(Exception e) {
    System.err.println("\nFull exception chain:");
    Throwable cause = e;
    int depth = 0;
    while (cause != null) {
      System.err.println("  [" + depth + "] " + cause.getClass().getName());
      System.err.println("      Message: " + cause.getMessage());
      cause = cause.getCause();
      depth++;
    }
  }

  // Custom KeyDeserializer that calls Enum.valueOf() directly
  static class JobTypeKeyDeserializer extends KeyDeserializer {
    @Override
    public Object deserializeKey(String key, DeserializationContext ctxt)
        throws JsonProcessingException {
      // Convert to uppercase to match enum naming convention
      String enumKey = key.toUpperCase();
      // This will throw IllegalArgumentException: No enum constant...
      return JobType.valueOf(enumKey);
    }
  }
}

enum JobType {
  B2B_A_EXPORT,
}

class TemporalWorkflowContext {
  private Map<JobType, String> dagInfo;

  public Map<JobType, String> getDagInfo() {
    return dagInfo;
  }

  public void setDagInfo(Map<JobType, String> dagInfo) {
    this.dagInfo = dagInfo;
  }
}
