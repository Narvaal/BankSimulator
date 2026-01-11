package br.com.ale.service.crypto;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
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
            throw new RuntimeException("Error saving private key", e);
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
                            } catch (IOException ignored) {}
                        });
            }
        } catch (IOException e) {
            throw new RuntimeException("Error deleting private key", e);
        }
    }
}
