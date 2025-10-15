package io.temporal.zd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

/** Utility to export tickets and comments from a view to JSON files. */
public class ZendeskExport {
  private static final Set<String> PII_FIELDS = Set.of("name", "address", "email", "phone");

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

    // Collect organization IDs from tickets and fetch org details once
    List<Long> orgIds = new ArrayList<>();
    for (ObjectNode b : bundles) {
      JsonNode ticket = b.path("ticket");
      if (ticket != null) {
        long orgId = ticket.path("organization_id").asLong(0);
        if (orgId != 0) orgIds.add(orgId);
      }
    }
    // Remove duplicates while preserving order
    List<Long> dedupedOrgIds = orgIds.stream().distinct().toList();
    List<JsonNode> orgs = client.getOrganizationsByIds(dedupedOrgIds);
    // Build a simple id -> org map
    java.util.Map<Long, JsonNode> orgById = new java.util.HashMap<>();
    for (JsonNode o : orgs) {
      long id = o.path("id").asLong(0);
      if (id != 0) orgById.put(id, o);
    }

    for (ObjectNode b : bundles) {
      // Enrich ticket with organization name and external id if available
      JsonNode ticket = b.path("ticket");
      if (ticket != null && ticket.isObject()) {
        long orgId = ticket.path("organization_id").asLong(0);
        JsonNode org = orgById.get(orgId);
        if (org != null && org.isObject()) {
          String orgName = org.path("name").asText(null);
          String orgExternalId = org.path("external_id").asText(null);
          ((ObjectNode) ticket).put("organization_name", orgName);
          ((ObjectNode) ticket).put("organization_external_id", orgExternalId);
        }
      }
      // Remove personal information before writing
      sanitizeInPlace(b);
      long id = b.path("ticket").path("id").asLong();
      Path out = targetDir.resolve("ticket-" + id + ".json");
      mapper.writerWithDefaultPrettyPrinter().writeValue(out.toFile(), b);
    }
  }

  // Recursively remove likely personal information fields.
  private void sanitizeInPlace(JsonNode node) {
    if (node == null) return;
    if (node.isObject()) {
      ObjectNode obj = (ObjectNode) node;
      List<String> toRemove = new ArrayList<>();
      for (Iterator<String> it = obj.fieldNames(); it.hasNext(); ) {
        String field = it.next();
        JsonNode child = obj.get(field);

        if (PII_FIELDS.contains(field)) {
          toRemove.add(field);
          continue;
        }

        // Targeted cleanup for Zendesk via.source.{to,from}
        if ("via".equals(field) && child != null && child.isObject()) {
          JsonNode source = child.get("source");
          if (source != null && source.isObject()) {
            JsonNode to = source.get("to");
            if (to != null && to.isObject()) {
              ((ObjectNode) to).remove(PII_FIELDS);
            }
            JsonNode from = source.get("from");
            if (from != null && from.isObject()) {
              ((ObjectNode) from).remove(PII_FIELDS);
            }
          }
        }

        sanitizeInPlace(child);
      }
      if (!toRemove.isEmpty()) {
        obj.remove(toRemove);
      }
    } else if (node.isArray()) {
      ArrayNode arr = (ArrayNode) node;
      for (JsonNode el : arr) {
        sanitizeInPlace(el);
      }
    }
  }
}
