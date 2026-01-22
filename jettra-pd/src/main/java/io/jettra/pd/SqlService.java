package io.jettra.pd;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jboss.logging.Logger;

import io.jettra.pd.auth.TokenUtils;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;

@ApplicationScoped
public class SqlService {
    private static final Logger LOG = Logger.getLogger(SqlService.class);
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Inject
    PlacementDriverService pdService;

    public String executeSql(String sql, String username, boolean resolveRefs) {
        LOG.infof("Executing SQL for user %s (resolveRefs=%b): %s", username, resolveRefs, sql);

        String lowerSql = sql.trim().toLowerCase();

        if (lowerSql.startsWith("select")) {
            return handleSelect(sql, resolveRefs);
        } else if (lowerSql.startsWith("insert")) {
            return handleInsert(sql);
        } else if (lowerSql.startsWith("update")) {
            return handleUpdate(sql);
        } else if (lowerSql.startsWith("delete")) {
            return handleDelete(sql);
        } else {
            return "{\"error\":\"Unsupported SQL command. Supported: SELECT, INSERT, UPDATE, DELETE.\"}";
        }
    }

    private String handleSelect(String sql, boolean resolveRefs) {
        // SELECT * FROM db.collection
        Pattern pattern = Pattern.compile("(?i)select.*?from\\s+([\\w.]+)(.*)");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String fullTable = matcher.group(1);
            String[] parts = fullTable.split("\\.");
            String db = (parts.length > 1) ? parts[0] : "default";
            String col = (parts.length > 1) ? parts[1] : parts[0];

            String path = col + (resolveRefs ? "?resolveRefs=true" : "");
            return routeToStore(db, path, "GET", null);
        }
        return "{\"error\":\"Invalid SELECT syntax. Use: SELECT * FROM db.collection\"}";
    }

    private String handleInsert(String sql) {
        // INSERT INTO db.col (id, name) VALUES ('v1', 'v2')
        // Simplified: we'll match table and values
        Pattern pattern = Pattern.compile("(?i)insert\\s+into\\s+([\\w.]+).*?values\\s*\\((.*)\\)");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String fullTable = matcher.group(1);
            String valuesRaw = matcher.group(2);

            String[] parts = fullTable.split("\\.");
            String db = (parts.length > 1) ? parts[0] : "default";
            String col = (parts.length > 1) ? parts[1] : parts[0];

            // Convert simple values to JSON (very basic hack)
            // Expecting 'val1', 'val2', ...
            String[] vals = valuesRaw.split(",");
            StringBuilder json = new StringBuilder("{");
            for (int i = 0; i < vals.length; i++) {
                String v = vals[i].trim().replace("'", "\"");
                json.append("\"field").append(i).append("\":").append(v);
                if (i < vals.length - 1)
                    json.append(",");
            }
            json.append("}");

            return routeToStore(db, col, "POST", json.toString());
        }
        return "{\"error\":\"Invalid INSERT syntax. Use: INSERT INTO db.col VALUES ('v1', 'v2')\"}";
    }

    private String handleUpdate(String sql) {
        // UPDATE db.col SET f1='v1' WHERE id='id1'
        Pattern pattern = Pattern.compile("(?i)update\\s+([\\w.]+)\\s+set\\s+(.*?)\\s+where\\s+id\\s*=\\s*'(.*?)'");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String fullTable = matcher.group(1);
            String setClause = matcher.group(2);
            String id = matcher.group(3);

            String[] parts = fullTable.split("\\.");
            String db = (parts.length > 1) ? parts[0] : "default";
            String col = (parts.length > 1) ? parts[1] : parts[0];

            // Basic JSON for update
            String[] setParts = setClause.split("=");
            String field = setParts[0].trim();
            String value = setParts[1].trim().replace("'", "\"");
            String json = String.format("{\"%s\": %s}", field, value);

            // Add jettraID param for update
            String path = col + "?jettraID=" + id;
            return routeToStore(db, path, "POST", json);
        }
        return "{\"error\":\"Invalid UPDATE syntax. Use: UPDATE db.col SET f1='v1' WHERE id='id1'\"}";
    }

    private String handleDelete(String sql) {
        // DELETE FROM db.col WHERE id='id1'
        Pattern pattern = Pattern.compile("(?i)delete\\s+from\\s+([\\w.]+)\\s+where\\s+id\\s*=\\s*'(.*?)'");
        Matcher matcher = pattern.matcher(sql);

        if (matcher.find()) {
            String fullTable = matcher.group(1);
            String id = matcher.group(2);

            String[] parts = fullTable.split("\\.");
            String db = (parts.length > 1) ? parts[0] : "default";
            String col = (parts.length > 1) ? parts[1] : parts[0];

            return routeToStore(db, col + "/" + id, "DELETE", null);
        }
        return "{\"error\":\"Invalid DELETE syntax. Use: DELETE FROM db.col WHERE id='id1'\"}";
    }

    private String routeToStore(String db, String path, String method, String body) {
        // Find a storage node
        NodeMetadata storeNode = pdService.getNodes().values().stream()
                .filter(n -> "ONLINE".equals(n.status()) && "STORAGE".equals(n.role()))
                .findFirst()
                .orElse(null);

        if (storeNode == null) {
            return "{\"error\":\"No online storage nodes available\"}";
        }

        try {
            String targetUrl = "http://" + storeNode.address() + "/api/v1/document/" + path;
            LOG.infof("Routing SQL as %s to %s", method, targetUrl);

            String token = TokenUtils.generateToken("system-pd", java.util.Set.of("admin", "system"));

            HttpRequest.Builder builder = HttpRequest.newBuilder()
                    .uri(URI.create(targetUrl))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json");

            if ("GET".equals(method)) {
                builder.GET();
            } else if ("POST".equals(method)) {
                builder.POST(HttpRequest.BodyPublishers.ofString(body != null ? body : "{}"));
            } else if ("DELETE".equals(method)) {
                builder.DELETE();
            }

            HttpResponse<String> response = httpClient.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (Exception e) {
            LOG.error("Failed to route SQL to store", e);
            return "{\"error\":\"Internal error routing to store: " + e.getMessage() + "\"}";
        }
    }
}
