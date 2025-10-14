package io.temporal.zd;

import java.nio.file.Path;
import java.nio.file.Paths;

public class Main {
  public static void main(String[] args) {

    System.out.println("Starting temporal zd");

    // Prefer env vars to avoid putting secrets in args
    String baseUrl = getenv("ZD_BASE_URL");
    String email = getenv("ZD_EMAIL");
    String apiToken = getenv("ZD_API_TOKEN");
    String password = getenv("ZD_PASSWORD");
    String useApiTokenEnv = getenv("ZD_USE_API_TOKEN");
    String viewIdStr = getenv("ZD_VIEW_ID");
    String outDirStr = getenvOrDefault("ZD_OUT_DIR", "zd-out");

    boolean useApiToken =
        (useApiTokenEnv == null && apiToken != null) // default to token if present
            || (useApiTokenEnv != null && useApiTokenEnv.equalsIgnoreCase("true"));

    if (isBlank(baseUrl)
        || isBlank(email)
        || isBlank(viewIdStr)
        || (useApiToken && isBlank(apiToken))
        || (!useApiToken && isBlank(password))) {
      printUsage();
      System.exit(2);
      return;
    }

    long viewId;
    try {
      viewId = Long.parseLong(viewIdStr);
    } catch (NumberFormatException nfe) {
      System.err.println("Invalid ZD_VIEW_ID: must be a number");
      System.exit(2);
      return;
    }

    String secret = useApiToken ? apiToken : password;
    Path outDir = Paths.get(outDirStr);

    try {
      ZendeskClient client = new ZendeskClient(baseUrl, email, secret, useApiToken);
      ZendeskExport export = new ZendeskExport(client);
      long start = System.currentTimeMillis();
      System.out.println("Exporting view " + viewId + " to " + outDir.toAbsolutePath());
      export.exportViewTicketsWithComments(viewId, outDir);
      long tookMs = System.currentTimeMillis() - start;
      System.out.println("Export complete in " + tookMs + " ms.");
    } catch (Exception e) {
      System.err.println("Error: " + e.getMessage());
      e.printStackTrace(System.err);
      System.exit(1);
    }
  }

  private static void printUsage() {
    System.err.println("Usage via environment variables:");
    System.err.println("  ZD_BASE_URL      e.g. https://your-subdomain.zendesk.com");
    System.err.println("  ZD_EMAIL         your Zendesk email");
    System.err.println("  ZD_API_TOKEN     Zendesk API token (if using token auth)");
    System.err.println("  ZD_PASSWORD      Zendesk password (if using password auth)");
    System.err.println("  ZD_USE_API_TOKEN true|false (defaults true if ZD_API_TOKEN is set)");
    System.err.println("  ZD_VIEW_ID       numeric view id to export");
    System.err.println("  ZD_OUT_DIR       output directory (default: zd-out)");
  }

  private static String getenv(String key) {
    String v = System.getenv(key);
    return v != null ? v.trim() : null;
  }

  private static String getenvOrDefault(String key, String def) {
    String v = getenv(key);
    return v == null || v.isEmpty() ? def : v;
  }

  private static boolean isBlank(String s) {
    return s == null || s.trim().isEmpty();
  }
}
