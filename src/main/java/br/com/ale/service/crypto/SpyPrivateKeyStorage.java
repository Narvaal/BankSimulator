package br.com.ale.service.crypto;

import java.security.KeyFactory;
import java.security.PrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;

public class SpyPrivateKeyStorage implements PrivateKeyStorage {

    private long savedAccountId;
    private byte[] savedPrivateKey;
    private boolean saveCalled = false;

    @Override
    public void save(long accountId, byte[] privateKey) {
        this.savedAccountId = accountId;
        this.savedPrivateKey = privateKey;
        this.saveCalled = true;
    }

    @Override
    public void delete(long accountId) {
        // not needed for this test
    }

    @Override
    public PrivateKey get(long accountId) {
        if (!saveCalled || savedPrivateKey == null) {
            return null;
        }

        if (accountId != savedAccountId) {
            return null;
        }

        try {
            PKCS8EncodedKeySpec spec =
                    new PKCS8EncodedKeySpec(savedPrivateKey);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");

            return keyFactory.generatePrivate(spec);

        } catch (Exception e) {
            throw new RuntimeException("Error reconstructing private key for test", e);
        }
    }

    public boolean wasSaveCalled() {
        return saveCalled;
    }

    public long getSavedAccountId() {
        return savedAccountId;
    }

    public byte[] getSavedPrivateKey() {
        return savedPrivateKey;
    }
}
