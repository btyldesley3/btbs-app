package com.btbs.application.accounts;

import com.btbs.domain.accounts.AccountRepository;
import com.btbs.domain.shared.id.AccountId;
import org.springframework.stereotype.Service;

@Service
public class GetBalanceUseCase {

    private final AccountRepository accounts;

    public GetBalanceUseCase(AccountRepository accounts) {
        this.accounts = accounts;
    }

    public String execute(AccountId id) {
        var account = accounts.findById(id).orElseThrow();
        return account.balance().toString();
    }

}
