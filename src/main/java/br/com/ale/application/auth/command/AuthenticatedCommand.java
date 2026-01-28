package br.com.ale.application.auth.command;

import java.security.PublicKey;

public record AuthenticatedCommand(String token, PublicKey publicKey) {
}
