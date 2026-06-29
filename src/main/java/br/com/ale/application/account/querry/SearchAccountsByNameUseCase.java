package br.com.ale.application.account.querry;

import br.com.ale.domain.exception.BusinessRuleException;
import br.com.ale.dto.PublicProfilePageView;
import br.com.ale.service.account.AccountService;

public class SearchAccountsByNameUseCase {

    private final AccountService accountService;

    public SearchAccountsByNameUseCase(AccountService accountService) {
        this.accountService = accountService;
    }

    public PublicProfilePageView execute(String query, int page, int pageSize) {
        if (query == null || query.trim().length() < 2) {
            throw new BusinessRuleException("Search query must have at least 2 characters");
        }
        return accountService.searchByName(query.trim(), page, pageSize);
    }
}
