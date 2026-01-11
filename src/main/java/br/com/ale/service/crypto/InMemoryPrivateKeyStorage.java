package br.com.ale.service.crypto;

public class InMemoryPrivateKeyStorage implements PrivateKeyStorage {

    @Override
    public void save(long accountId, byte[] privateKey) {
        // NO-OP
    }

    @Override
    public void delete(long accountId) {
        // NO-OP
    }
}
