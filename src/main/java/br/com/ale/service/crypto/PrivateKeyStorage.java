package br.com.ale.service.crypto;

import java.security.PrivateKey;

public interface PrivateKeyStorage {

    void save(long accountId, byte[] privateKey);

    void delete(long accountId);

    PrivateKey get(long accountId);
}
