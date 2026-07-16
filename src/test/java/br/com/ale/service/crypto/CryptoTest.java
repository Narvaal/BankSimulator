package br.com.ale.service.crypto;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.security.KeyPair;
import java.security.PrivateKey;
import java.security.Signature;
import java.time.Instant;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class CryptoTest {

  private final KeyPairService keyPairService = new KeyPairService();

  private static String sign(String message, PrivateKey privateKey) throws Exception {
    Signature signature = Signature.getInstance("SHA256withRSA");
    signature.initSign(privateKey);
    signature.update(message.getBytes());
    return Base64.getEncoder().encodeToString(signature.sign());
  }

  @Test
  void keyPairServiceShouldGenerateRsa2048AndEncode() {
    KeyPair pair = keyPairService.generate();

    assertEquals("RSA", pair.getPublic().getAlgorithm());
    assertFalse(keyPairService.encodePublicKey(pair).isBlank());
    assertFalse(keyPairService.encodePrivateKey(pair).isBlank());
  }

  @Test
  void signatureVerifierShouldAcceptValidAndRejectTamperedMessage() throws Exception {
    KeyPair pair = keyPairService.generate();
    String message =
        TransactionMessageBuilder.build(1L, 2L, new BigDecimal("10.00"), Instant.now());

    String signature = sign(message, pair.getPrivate());

    assertTrue(SignatureVerifier.verify(message, signature, pair.getPublic()));
    assertFalse(SignatureVerifier.verify(message + "tampered", signature, pair.getPublic()));
  }

  @Test
  void signatureVerifierShouldThrowOnGarbageSignature() {
    KeyPair pair = keyPairService.generate();
    assertThrows(
        RuntimeException.class,
        () -> SignatureVerifier.verify("msg", "not-base64!!", pair.getPublic()));
  }

  @Test
  void transactionMessageBuilderShouldFormatAllFields() {
    Instant ts = Instant.parse("2026-01-01T00:00:00Z");
    String message = TransactionMessageBuilder.build(1L, 2L, new BigDecimal("10.50"), ts);

    assertEquals("from=1|to=2|amount=10.50|timestamp=2026-01-01T00:00:00Z", message);
  }

  @Test
  void inMemoryStorageShouldSaveGetAndDelete() {
    KeyPair pair = keyPairService.generate();
    InMemoryPrivateKeyStorage storage = new InMemoryPrivateKeyStorage();

    storage.save(42L, pair.getPrivate().getEncoded());
    assertNotNull(storage.get(42L));

    storage.delete(42L);
    assertNull(storage.get(42L));
  }

  @Test
  void fileStorageShouldRoundTripPrivateKey() throws Exception {
    KeyPair pair = keyPairService.generate();
    FilePrivateKeyStorage storage = new FilePrivateKeyStorage();
    long accountId = System.nanoTime();

    try {
      storage.save(accountId, pair.getPrivate().getEncoded());

      PrivateKey restored = storage.get(accountId);
      assertEquals("RSA", restored.getAlgorithm());

      String message = "round-trip";
      String signature = sign(message, restored);
      assertTrue(SignatureVerifier.verify(message, signature, pair.getPublic()));

    } finally {
      storage.delete(accountId);
    }

    assertThrows(RuntimeException.class, () -> storage.get(accountId));
  }

  @Test
  void fileStorageDeleteShouldIgnoreMissingDirectory() {
    new FilePrivateKeyStorage().delete(-12345L);
  }
}
