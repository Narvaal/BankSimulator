package br.com.ale.service.crypto;

public interface PrivateKeyStorage {

    void save(long accountId, byte[] privateKey);

    void delete(long accountId);
}
