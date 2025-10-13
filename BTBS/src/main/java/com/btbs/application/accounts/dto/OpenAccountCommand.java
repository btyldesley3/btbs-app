package com.btbs.application.accounts.dto;

import com.btbs.domain.accounts.AccountNumber;
import com.btbs.domain.accounts.AccountType;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record OpenAccountCommand(
        UUID customerId,
        AccountNumber accountNumber,
        Currency currency,
        AccountType accType,
        BigDecimal openingBalance,
        boolean overdraftEnabled,
        BigDecimal overdraftLimit
) {
}
