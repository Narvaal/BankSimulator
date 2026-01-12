package br.com.ale.service.crypto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.PrivateKey;
import java.util.Base64;

public class FilePrivateKeyStorage implements PrivateKeyStorage {

    private static final String BASE_DIR = "keys";

    @Override
    public void save(long accountId, byte[] privateKey) {
        try {
            Path dir = Path.of(BASE_DIR, "account-" + accountId);
            Files.createDirectories(dir);

            Path keyFile = dir.resolve("private.key");

            String encoded = Base64.getEncoder().encodeToString(privateKey);
            Files.writeString(keyFile, encoded);

        } catch (IOException e) {
            throw new RuntimeException(
                    "Saving private key" + "[accountId=" + accountId + "]",
                    e
            );
        }
    }

    @Override
    public void delete(long accountId) {
        try {
            Path dir = Path.of(BASE_DIR, "account-" + accountId);
            if (Files.exists(dir)) {
                Files.walk(dir)
                        .sorted((a, b) -> b.compareTo(a)) // delete children first
                        .forEach(path -> {
                            try {
                                Files.delete(path);
                            } catch (IOException ignored) {
                            }
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException(
                    "Deleting private key" + "[accountId=" + accountId + "]",
                    e
            );
        }
    }

    @Override
    public PrivateKey get(long accountId) {

        try {
            Path keyFile = Path.of(
                    BASE_DIR,
                    "account-" + accountId,
                    "private.key"
            );

            if (!Files.exists(keyFile)) {
                throw new RuntimeException(
                        "Private key not found " + "[accountId=" + accountId + "]"
                );
            }

            String encodedKey = Files.readString(keyFile);

            byte[] keyBytes =
                    Base64.getDecoder().decode(encodedKey);

            var keySpec = new java.security.spec.PKCS8EncodedKeySpec(keyBytes);

            var keyFactory = java.security.KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(keySpec);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Private key not loading " + "[accountId=" + accountId + "]"
            );
        }
    }
}
