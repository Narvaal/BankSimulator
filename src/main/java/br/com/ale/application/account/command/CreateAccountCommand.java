package br.com.ale.application.account.command;

import br.com.ale.domain.client.Provider;

public record CreateAccountCommand(String name, String email, String password) {
}
