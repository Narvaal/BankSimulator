package br.com.ale.application.auth.command;

public record GoogleLoginCommand(
        String idToken
) {
}
