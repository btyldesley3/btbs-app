package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.GetAccountBalanceQuery;
import com.btbs.domain.accounts.AccountRepository;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.value.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GetAccountBalanceUseCase {

    private final AccountRepository accountRepository;

    public GetAccountBalanceUseCase(AccountRepository accounts) {
        this.accountRepository = accounts;
    }

    @Transactional(readOnly = true)
    public Money getAccountBalance(GetAccountBalanceQuery query) {
        var id = new AccountId(query.accountId());
        var account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        return account.balance();
    }

}
