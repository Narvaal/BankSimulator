package br.com.ale.dto;

import java.math.BigDecimal;

public record DepositAccountApiRequest(BigDecimal amount, String token) {
}
