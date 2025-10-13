package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.OpenAccountCommand;
import com.btbs.domain.accounts.AccountRepository;
import com.btbs.domain.accounts.AccountStatus;
import com.btbs.domain.accounts.CustomerAccount;
import com.btbs.domain.accounts.OverdraftPolicy;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class OpenAccountUseCase {

    private final AccountRepository accountRepository;

    public OpenAccountUseCase(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional //May delegate exception to a GlobalExceptionHandler once custom exceptions created
    public AccountId openAccount(OpenAccountCommand cmd) {
        if (accountRepository.existsByAccountNumber(cmd.accountNumber())) {
            throw new IllegalArgumentException("Account number already exists");
        }
        var currency = cmd.currency();
        var opening = Money.of(cmd.openingBalance(), currency);
        var odPolicy = new OverdraftPolicy(cmd.overdraftEnabled(),
                cmd.overdraftEnabled() ? cmd.overdraftLimit() : BigDecimal.ZERO);

        var customerAccount = new CustomerAccount(
                AccountId.newId(),
                new CustomerId(cmd.customerId()),
                cmd.accountNumber(),
                currency,
                cmd.accType(),
                AccountStatus.ACTIVE,
                opening,
                odPolicy,
                0L
        );
        accountRepository.save(customerAccount);
        return customerAccount.id();
    }

}
