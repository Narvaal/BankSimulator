package br.com.ale.application;
import br.com.ale.dto.CreateAccountRequest;
import br.com.ale.dto.CreateClientRequest;
import br.com.ale.dto.UpdateAccountRequest;
import br.com.ale.dto.UpdateClientRequest;
import br.com.ale.infrastructure.db.DefaultConnectionProvider;
import br.com.ale.infrastructure.db.TestConnectionProvider;
import br.com.ale.service.AccountService;
import br.com.ale.service.ClientService;

public class Main {
    public static void main(String[] args) {

        String name = "ALESSANDRO BEZERRA";
        String document = "2131232314";
        ClientService clientService = new ClientService(new DefaultConnectionProvider());
        clientService.createClient(new CreateClientRequest(name, document));
        //clientService.updateClient(new UpdateClientRequest(1, name));


        String accountNumber = "12313123";
        String accountType = "credit";
        String status = "dead";
        //AccountService accountService = new AccountService(new TestConnectionProvider());
        //accountService.createAccount( new CreateAccountRequest(1, accountNumber, accountType, status));
        //accountService.updateAccount( new UpdateAccountRequest(1, accountNumber, accountType, status));

        //System.out.println(account.getId());
    }
}
