package br.com.ale.dto;

public record CreateAccountApiRequest(String name, String email, String password) {
}
