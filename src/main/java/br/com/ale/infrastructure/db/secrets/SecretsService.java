package br.com.ale.infrastructure.db.secrets;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Map;

@Service
public class SecretsService {

    private final SecretsManagerClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    private String cachedPassword;
    private long lastFetchTime = 0;

    private static final long TTL = 5 * 60 * 1000;

    public SecretsService(SecretsManagerClient client) {
        this.client = client;
    }

    public synchronized String getDbPassword() {
        long now = System.currentTimeMillis();

        if (cachedPassword == null || (now - lastFetchTime) > TTL) {
            cachedPassword = fetchPassword();
            lastFetchTime = now;
        }

        return cachedPassword;
    }

    private String fetchPassword() {
        try {
            String secretName = "rds!db-afd0eb8d-fa08-474e-b202-51f06b07506b";

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            var response = client.getSecretValue(request);

            Map<String, Object> json =
                    mapper.readValue(response.secretString(), Map.class);

            return (String) json.get("password");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar secret do AWS", e);
        }
    }
}