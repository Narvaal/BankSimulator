package br.com.ale.service.crypto;

import java.math.BigDecimal;
import java.time.Instant;

public class TransactionMessageBuilder {

    public static String build(
            Long fromAccountId,
            Long toAccountId,
            BigDecimal amount,
            Instant timestamp
    ) {
        return String.format(
                "from=%d|to=%d|amount=%s|timestamp=%s",
                fromAccountId,
                toAccountId,
                amount.toPlainString(),
                timestamp.toString()
        );
    }
}
