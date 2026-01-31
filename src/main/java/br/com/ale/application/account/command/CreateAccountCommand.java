package br.com.ale.application.account.command;

public record CreateAccountCommand(String name, String document, String password) {
}
