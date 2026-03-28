package br.com.ale.application.account.command;

public record ChangePasswordCommand(String password, String token) {
}
