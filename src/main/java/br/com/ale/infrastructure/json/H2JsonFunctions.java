package br.com.ale.infrastructure.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;

public class H2JsonFunctions {

  private static final ObjectMapper mapper = new ObjectMapper();

  @SuppressWarnings("unchecked")
  public static String jsonValue(String json, String path) throws Exception {
    if (json == null || path == null) return null;

    // Handle H2 double-encoding: {"name":"Tesla"} may arrive as '{"name":"Tesla"}'
    String actual = json;
    if (json.startsWith("\"") && json.endsWith("\"")) {
      actual = mapper.readValue(json, String.class);
    }

    // path format: $.key (e.g. $.name)
    String key = path.startsWith("$.") ? path.substring(2) : path;

    Object node = mapper.readValue(actual, Map.class).get(key);
    return node != null ? node.toString() : null;
  }
}
