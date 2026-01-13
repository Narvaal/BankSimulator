package br.com.ale.infrastructure.json;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.InputStream;
import java.util.List;

public class JsonUtils {

    private static final ObjectMapper mapper = new ObjectMapper();

    public static List<String> readArray(String resourcePath) {
        try (InputStream is =
                     JsonUtils.class
                             .getClassLoader()
                             .getResourceAsStream(resourcePath)) {

            if (is == null) {
                throw new RuntimeException("File not found: " + resourcePath);
            }

            return mapper.readValue(
                    is,
                    mapper.getTypeFactory()
                            .constructCollectionType(List.class, String.class)
            );

        } catch (Exception e) {
            throw new RuntimeException("Error reading JSON file: " + resourcePath, e);
        }
    }
}
