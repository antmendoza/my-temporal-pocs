package io.temporal.zd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;

/**
 * Minimal Zendesk API client to: - List tickets in a view - Fetch comments for each ticket
 *
 * <p>Auth: Basic auth using either email/password or email/token + apiToken. For API token, pass
 * email as username and set useApiToken=true; the client will send username as "email/token" with
 * apiToken as the password.
 */
public class ZendeskClient {
  private final String baseUrl; // e.g. https://your-subdomain.zendesk.com
  private final String authHeader;
  private final HttpClient http;
  private final ObjectMapper mapper = new ObjectMapper();
  private final long minIntervalMillis;
  private long lastRequestAtMs = 0L;

  public ZendeskClient(String baseUrl, String email, String secret, boolean useApiToken) {
    this(baseUrl, email, secret, useApiToken, Duration.ofSeconds(30));
  }

  public ZendeskClient(
      String baseUrl, String email, String secret, boolean useApiToken, Duration timeout) {
    Objects.requireNonNull(baseUrl, "baseUrl");
    Objects.requireNonNull(email, "email");
    Objects.requireNonNull(secret, "secret");

    String normalized =
        baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    this.baseUrl = normalized;
    String username = useApiToken ? email + "/token" : email;
    String basic =
        Base64.getEncoder()
            .encodeToString((username + ":" + secret).getBytes(StandardCharsets.UTF_8));
    this.authHeader = "Basic " + basic;
    this.http =
        HttpClient.newBuilder()
            .connectTimeout(timeout)
            .version(HttpClient.Version.HTTP_1_1)
            .build();
    this.minIntervalMillis = 1000L; // guardrail: max 1 request per second
  }

  /**
   * Lists all tickets in a Zendesk view, following pagination.
   *
   * @param viewId Zendesk view id
   * @return List of ticket objects (as JsonNode)
   */
  public List<JsonNode> listTicketsInView(long viewId) throws IOException, InterruptedException {
    List<JsonNode> tickets = new ArrayList<>();
    String url = baseUrl + "/api/v2/views/" + viewId + "/tickets.json";

    while (url != null) {
      JsonNode body = sendGet(url);
      ArrayNode pageTickets = asArray(body.get("tickets"));
      if (pageTickets != null) {
        for (JsonNode t : pageTickets) {
          tickets.add(t);
        }
      }
      url = asTextOrNull(body.get("next_page"));
    }
    return tickets;
  }

  /** Fetch all comments for a ticket, following pagination. */
  public List<JsonNode> listTicketComments(long ticketId) throws IOException, InterruptedException {
    List<JsonNode> comments = new ArrayList<>();
    String url = baseUrl + "/api/v2/tickets/" + ticketId + "/comments.json";

    while (url != null) {
      JsonNode body = sendGet(url);
      ArrayNode pageComments = asArray(body.get("comments"));
      if (pageComments != null) {
        for (JsonNode c : pageComments) {
          comments.add(c);
        }
      }
      url = asTextOrNull(body.get("next_page"));
    }
    return comments;
  }

  /**
   * Convenience method: for a view, fetch each ticket and its comments. Returns an array of
   * objects: { ticket: {...}, comments: [...] }
   */
  public List<ObjectNode> listViewTicketsWithComments(long viewId)
      throws IOException, InterruptedException {
    List<JsonNode> tickets = listTicketsInView(viewId);
    List<ObjectNode> out = new ArrayList<>();
    for (JsonNode t : tickets) {
      long id = t.path("id").asLong();
      List<JsonNode> comments = listTicketComments(id);
      ObjectNode bundle = mapper.createObjectNode();
      bundle.set("ticket", t);
      ArrayNode commentsArr = mapper.createArrayNode();
      comments.forEach(commentsArr::add);
      bundle.set("comments", commentsArr);
      out.add(bundle);
    }
    return out;
  }

  /**
   * Fetch organizations by IDs using Zendesk show_many endpoint. Returns a list of organization
   * objects. Caller can map by {@code id}.
   */
  public List<JsonNode> getOrganizationsByIds(List<Long> ids)
      throws IOException, InterruptedException {
    List<JsonNode> orgs = new ArrayList<>();
    if (ids == null || ids.isEmpty()) return orgs;

    // Zendesk show_many supports up to 100 ids at a time.
    final int BATCH = 100;
    for (int i = 0; i < ids.size(); i += BATCH) {
      int end = Math.min(ids.size(), i + BATCH);
      List<Long> batch = ids.subList(i, end);
      StringBuilder sb = new StringBuilder();
      for (int j = 0; j < batch.size(); j++) {
        if (j > 0) sb.append(',');
        sb.append(batch.get(j));
      }
      String url = baseUrl + "/api/v2/organizations/show_many.json?ids=" + sb;
      JsonNode body = sendGet(url);
      ArrayNode arr = asArray(body.get("organizations"));
      if (arr != null) {
        for (JsonNode o : arr) orgs.add(o);
      }
    }
    return orgs;
  }

  private JsonNode sendGet(String url) throws IOException, InterruptedException {

    System.out.println("url " + url);

    HttpRequest req =
        HttpRequest.newBuilder()
            .uri(URI.create(url))
            .timeout(Duration.ofSeconds(60))
            .header("Authorization", authHeader)
            .header("Accept", "application/json")
            .GET()
            .build();

    throttle();
    HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
    int code = resp.statusCode();
    if (code == 429) { // rate limited; attempt naive retry with backoff header if present
      String retryAfter = resp.headers().firstValue("Retry-After").orElse("1");
      try {
        long delaySec = Long.parseLong(retryAfter);
        Thread.sleep(Math.max(1, delaySec) * 1000L);
      } catch (NumberFormatException ignored) {
        Thread.sleep(1000);
      }
      throttle();
      resp = http.send(req, HttpResponse.BodyHandlers.ofString());
      code = resp.statusCode();
    }
    if (code < 200 || code >= 300) {
      String msg = truncate(resp.body(), 800);
      throw new IOException("Zendesk API request failed: " + code + " " + msg + " (" + url + ")");
    }
    return mapper.readTree(resp.body());
  }

  // Ensure we do not exceed one request per second
  private synchronized void throttle() throws InterruptedException {
    long now = System.currentTimeMillis();
    long elapsed = now - lastRequestAtMs;
    long wait = minIntervalMillis - elapsed;
    if (lastRequestAtMs != 0 && wait > 0) {
      Thread.sleep(wait);
    }
    lastRequestAtMs = System.currentTimeMillis();
  }

  private static String truncate(String s, int max) {
    if (s == null) return "";
    return s.length() <= max ? s : s.substring(0, max) + "...";
  }

  private static ArrayNode asArray(JsonNode node) {
    return node != null && node.isArray() ? (ArrayNode) node : null;
  }

  private static String asTextOrNull(JsonNode node) {
    return node == null || node.isNull() ? null : node.asText(null);
  }
}
