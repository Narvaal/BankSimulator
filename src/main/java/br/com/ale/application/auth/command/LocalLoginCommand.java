package br.com.ale.application.auth.command;

public record LocalLoginCommand(String email, String password) {
}
