package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.WithdrawFundsCommand;
import com.btbs.domain.accounts.AccountRepository;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.value.Money;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class WithdrawFundsUseCase {

    private final AccountRepository accountRepository;

    public WithdrawFundsUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional
    public void withdrawFunds(WithdrawFundsCommand cmd) {
        var id = new AccountId(cmd.accountId());
        var account = accountRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found"));

        // Domain enforces overdraft policy & currency checks
        var updated = account.withdraw(Money.of(cmd.amount(), cmd.currency()), Instant.now());
        accountRepository.save(updated);

    }
}
