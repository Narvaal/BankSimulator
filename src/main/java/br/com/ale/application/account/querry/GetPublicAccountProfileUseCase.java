package br.com.ale.application.account.querry;

import br.com.ale.dto.PublicProfileResponse;
import br.com.ale.service.account.AccountService;

public class GetPublicAccountProfileUseCase {

  private final AccountService accountService;

  public GetPublicAccountProfileUseCase(AccountService accountService) {
    this.accountService = accountService;
  }

  public PublicProfileResponse execute(long accountId) {
    return accountService.getPublicProfile(accountId);
  }
}
