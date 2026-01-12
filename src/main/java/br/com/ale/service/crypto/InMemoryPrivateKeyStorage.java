package br.com.ale.service.crypto;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryPrivateKeyStorage implements PrivateKeyStorage {

    private final Map<Long, PrivateKey> storage = new ConcurrentHashMap<>();

    @Override
    public void save(long accountId, byte[] privateKeyBytes) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            PrivateKey privateKey = keyFactory.generatePrivate(
                    new PKCS8EncodedKeySpec(privateKeyBytes)
            );
            storage.put(accountId, privateKey);
        } catch (Exception e) {
            throw new RuntimeException(
                    "Saving private key in memory" + "[accountId=" + accountId + "]",
                    e
            );
        }
    }

    @Override
    public PrivateKey get(long accountId) {
        return storage.get(accountId);
    }

    @Override
    public void delete(long accountId) {
        storage.remove(accountId);
    }
}
