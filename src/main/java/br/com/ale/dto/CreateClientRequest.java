package br.com.ale.dto;

import br.com.ale.domain.client.Provider;

public record CreateClientRequest(String name, String email, String password, Provider provider,
                                  String providerId, boolean emailVerified, String picture)
{
}