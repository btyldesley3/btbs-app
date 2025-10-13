package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.TransferFundsCommand;
import com.btbs.domain.accounts.AccountRepository;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.value.Money;
import jakarta.transaction.Transactional;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
public class TransferFundsService {

    private final AccountRepository accountRepository;

    //May externalise this to config package and extend functionality
    private static final int MAX_RETRIES = 3;

    public TransferFundsService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    /**
     * Transfer funds between two accounts (same currency), with a small retry for optimistic locking conflicts.
     */
    @Transactional
    public void transferFunds(TransferFundsCommand cmd) {
        if (cmd.sourceAccountId().equals(cmd.destinationAccountId())) {
            throw new IllegalArgumentException("Source and destination cannot be the same account");
        }

        var amount = Money.of(cmd.amount(), cmd.currency());
        var now = Instant.now();

        int attempt = 0;
        while (true) {
            try {
                // Load fresh each attempt to get the latest versions
                var sourceAccountId = new AccountId(cmd.sourceAccountId());
                var destinationAccountId = new AccountId(cmd.destinationAccountId());

                var sourceAccount = accountRepository.findById(sourceAccountId).orElseThrow(() -> new IllegalArgumentException("Source account not found"));
                var destinationAccount = accountRepository.findById(destinationAccountId).orElseThrow(() -> new IllegalArgumentException("Destination account not found"));

                // Enforce same-currency transfer at application layer (domain also checks per-operation currency)
                if (!sourceAccount.currency().equals(destinationAccount.currency()) || !sourceAccount.currency().equals(amount.currency())) {
                    throw new IllegalArgumentException("Currency mismatch for transfer");
                }

                // Domain rules: withdraw then deposit (@Transactional: both succeed or neither)
                var debitedAccount = sourceAccount.withdraw(amount, now);
                var creditedAccount = destinationAccount.deposit(amount, now);

                // Persist both. With @Version in entities + version carried in domain, stale writers will fail.
                accountRepository.save(debitedAccount);
                accountRepository.save(creditedAccount);

                // Success
                return;

            } catch (OptimisticLockingFailureException e) {
                if (++attempt >= MAX_RETRIES) {
                    throw e; // give up
                }
                // loop to retry: next iteration reloads latest state
            }
        }
    }

}
