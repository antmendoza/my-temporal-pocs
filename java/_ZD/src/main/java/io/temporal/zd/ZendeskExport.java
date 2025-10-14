package io.temporal.zd;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/** Utility to export tickets and comments from a view to JSON files. */
public class ZendeskExport {
  private final ZendeskClient client;
  private final ObjectMapper mapper = new ObjectMapper();

  public ZendeskExport(ZendeskClient client) {
    this.client = client;
  }

  /**
   * Writes one JSON file per ticket into the target directory: ticket-<id>.json The JSON structure:
   * { "ticket": {...}, "comments": [ ... ] }
   */
  public void exportViewTicketsWithComments(long viewId, Path targetDir)
      throws IOException, InterruptedException {
    if (!Files.exists(targetDir)) {
      Files.createDirectories(targetDir);
    }

    List<ObjectNode> bundles = client.listViewTicketsWithComments(viewId);
    for (ObjectNode b : bundles) {
      long id = b.path("ticket").path("id").asLong();
      Path out = targetDir.resolve("ticket-" + id + ".json");
      mapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), b);
    }
  }
}
