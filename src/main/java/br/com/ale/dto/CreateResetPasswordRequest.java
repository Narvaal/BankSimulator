package br.com.ale.dto;

public record CreateResetPasswordRequest(String password, String token) {
}
