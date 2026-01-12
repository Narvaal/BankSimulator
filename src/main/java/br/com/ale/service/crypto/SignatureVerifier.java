package br.com.ale.service.crypto;

import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class SignatureVerifier {

    public static boolean verify(
            String message,
            String signatureBase64,
            PublicKey publicKey
    ) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(publicKey);
            signature.update(message.getBytes());

            byte[] signatureBytes =
                    Base64.getDecoder().decode(signatureBase64);

            return signature.verify(signatureBytes);

        } catch (Exception e) {
            throw new RuntimeException("Error - verifying signature", e);
        }
    }
}
