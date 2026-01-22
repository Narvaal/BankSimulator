package br.com.ale.service.account;

import br.com.ale.domain.client.Client;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

public class HashAccountNumberGenerator implements AccountNumberGenerator {

    private static final String PREFIX = "ACC-";

    @Override
    public String generate(Client client) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            String input = client.getDocument() + "|BANK|SALT";
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

            return PREFIX + bytesToHex(hash).substring(0, 12).toUpperCase();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate account number", e);
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
