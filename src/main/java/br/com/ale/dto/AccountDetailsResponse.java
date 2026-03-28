package br.com.ale.dto;

import br.com.ale.domain.account.AccountStatus;
import br.com.ale.domain.account.AccountType;
import br.com.ale.domain.client.Provider;

import java.math.BigDecimal;
import java.time.Instant;

public record AccountDetailsResponse(
        long id,
        long clientId,
        String accountNumber,
        AccountType accountType,
        BigDecimal balance,
        AccountStatus status,
        String publicKey,
        Instant createdAt,
        Instant updatedAt,
        Instant nextFreeAssetAt,

        String name,
        String picture,
        Boolean emailVerified,
        Provider provider

) {
}
