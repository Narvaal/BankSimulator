package br.com.ale.service;

import java.security.PrivateKey;
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
}
