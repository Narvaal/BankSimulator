package br.com.ale.service;

import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.util.Base64;

public class SignatureService {

    public static String sign(String message, PrivateKey privateKey) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initSign(privateKey);
            signature.update(message.getBytes());

            byte[] signedBytes = signature.sign();

            return Base64.getEncoder().encodeToString(signedBytes);

        } catch (Exception e) {


            throw new RuntimeException(
                    "Service error while generating signature " +
                            "[message=" + message + ", "
                            + "isPrivateKeyDestroyed=" + privateKey.isDestroyed() + "]",
                    e
            );
        }
    }

    public static boolean verify(String data, String sig, PublicKey key) {
        try {
            Signature signature = Signature.getInstance("SHA256withRSA");
            signature.initVerify(key);
            signature.update(data.getBytes());

            byte[] signatureBytes = Base64.getDecoder().decode(sig);
            return signature.verify(signatureBytes);

        } catch (Exception e) {
            throw new RuntimeException(
                    "Service error while verifying signature [data=" + data + "]",
                    e
            );
        }
    }
}
