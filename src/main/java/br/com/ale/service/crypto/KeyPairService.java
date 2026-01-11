package br.com.ale.service.crypto;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.Base64;

public class KeyPairService {

    public KeyPair generate() {
        try {
            KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
            generator.initialize(2048);
            return generator.generateKeyPair();
        } catch (Exception e) {
            throw new RuntimeException("Error generating RSA key pair", e);
        }
    }

    public String encodePublicKey(KeyPair keyPair) {
        return Base64.getEncoder()
                .encodeToString(keyPair.getPublic().getEncoded());
    }

    public String encodePrivateKey(KeyPair keyPair) {
        return Base64.getEncoder()
                .encodeToString(keyPair.getPrivate().getEncoded());
    }
}
