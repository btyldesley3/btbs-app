package com.btbs.application.accounts.dto;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record TransferFundsCommand(
        UUID sourceAccountId,
        UUID destinationAccountId,
        BigDecimal amount,
        Currency currency
) {
}
