package com.btbs.infrastructure.persistence.jpa.mappers;

import com.btbs.domain.accounts.AccountNumber;
import com.btbs.domain.accounts.CustomerAccount;
import com.btbs.domain.accounts.OverdraftPolicy;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import com.btbs.infrastructure.persistence.jpa.entities.AccountEntity;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public final class AccountMapper {

    private AccountMapper() {}

    public static AccountEntity toEntity(CustomerAccount domain) {
        if (domain == null) return null;

        return new AccountEntity(
                domain.id().value(),
                domain.customerId().value(),
                domain.accountNumber().value(),
                domain.currency().getCurrencyCode(),
                domain.type(),
                domain.status(),
                domain.balance().amount(),                   // BigDecimal
                domain.overdraft().enabled(),
                domain.overdraft().limitAbs()               // BigDecimal
        );
    }

    public static CustomerAccount toDomain(AccountEntity entity) {
        if (entity == null) return null;

        Currency currency = Currency.getInstance(entity.getCurrency());

        return new CustomerAccount(
                new AccountId(entity.getId()),
                new CustomerId(entity.getCustomerId()),
                new AccountNumber(entity.getAccountNumber()),
                currency,
                entity.getType(),
                entity.getStatus(),
                Money.of(entity.getBalanceAmount(), currency),
                new OverdraftPolicy(entity.isOverdraftEnabled(),
                        nonNull(entity.getOverdraftLimit()))
        );
    }

    private static BigDecimal nonNull(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    // convenience
    public static AccountId toAccountId(UUID id) {
        return id == null ? null : new AccountId(id);
    }
}

