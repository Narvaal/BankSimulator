package br.com.ale.dto;

import java.math.BigDecimal;

public record CreateAccountRequest (long clientId, String accountNumber, String accountType, String status) {
}
