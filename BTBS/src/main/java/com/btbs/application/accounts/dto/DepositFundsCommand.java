package com.btbs.application.accounts.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record DepositFundsCommand(
        UUID accountId,
        BigDecimal amount,
        Currency currency
) { }
