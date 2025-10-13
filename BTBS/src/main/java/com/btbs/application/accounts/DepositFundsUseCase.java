package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.DepositFundsCommand;
import com.btbs.domain.accounts.AccountRepository;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.value.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class DepositFundsUseCase {

    private final AccountRepository accountRepository;

    public DepositFundsUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void depositFunds(DepositFundsCommand cmd) {
        var id = new AccountId(cmd.accountId());
        var account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));
        var updated = account.deposit(Money.of(cmd.amount(), cmd.currency()), Instant.now());
        accountRepository.save(updated);
        // Optionally publish domain events after commit via an outbox/publisher port
    }
}
