package br.com.ale.infrastructure.json;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

public class JsonUtils {

  private static final ObjectMapper mapper = new ObjectMapper();

  public static List<String> readArray(String resourcePath) {
    try (InputStream is = JsonUtils.class.getClassLoader().getResourceAsStream(resourcePath)) {

      if (is == null) {
        throw new RuntimeException("File not found: " + resourcePath);
      }

      return mapper.readValue(
          is, mapper.getTypeFactory().constructCollectionType(List.class, String.class));

    } catch (Exception e) {
      throw new RuntimeException("Error reading JSON file: " + resourcePath, e);
    }
  }

  public static String toJson(Map<String, Object> map) {
    try {
      return mapper.writeValueAsString(map);
    } catch (Exception e) {
      throw new RuntimeException("Error serializing map to JSON", e);
    }
  }

  @SuppressWarnings("unchecked")
  public static Map<String, Object> fromJson(String json) {
    try {
      JsonNode node = mapper.readTree(json);
      // H2 JSON type returns objects as JSON-encoded strings (double-quoted)
      if (node.isTextual()) {
        return mapper.readValue(node.textValue(), Map.class);
      }
      return mapper.treeToValue(node, Map.class);
    } catch (Exception e) {
      throw new RuntimeException("Error deserializing JSON to map: " + json, e);
    }
  }
}
