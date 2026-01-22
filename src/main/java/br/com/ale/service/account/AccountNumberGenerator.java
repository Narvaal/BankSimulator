package br.com.ale.service.account;

import br.com.ale.domain.client.Client;

public interface AccountNumberGenerator {
    String generate(Client client);
}
