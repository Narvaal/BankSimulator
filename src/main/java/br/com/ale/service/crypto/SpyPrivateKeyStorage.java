package br.com.ale.service.crypto;

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
