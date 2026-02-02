package br.com.ale.application.account.command;

public record CreateAccountCommand(String name, String email, String password) {
}
