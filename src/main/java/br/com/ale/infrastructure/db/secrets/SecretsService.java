package br.com.ale.infrastructure.db.secrets;

import com.fasterxml.jackson.databind.ObjectMapper;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest;

import java.util.Map;

public class SecretsService {

    private final SecretsManagerClient client;
    private final ObjectMapper mapper = new ObjectMapper();

    public SecretsService() {
        this.client = SecretsManagerClient.builder()
                .region(Region.of("us-east-2"))
                .build();
    }

    public String getDbPassword() {
        try {
            String secretName = "rds!db-afd0eb8d-fa08-474e-b202-51f06b07506b";

            GetSecretValueRequest request = GetSecretValueRequest.builder()
                    .secretId(secretName)
                    .build();

            String secretString = client.getSecretValue(request).secretString();

            Map<String, Object> json = mapper.readValue(secretString, Map.class);

            return (String) json.get("password");

        } catch (Exception e) {
            throw new RuntimeException("Erro ao buscar secret do AWS", e);
        }
    }
}