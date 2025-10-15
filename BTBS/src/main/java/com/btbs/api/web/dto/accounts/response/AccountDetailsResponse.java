package com.btbs.api.web.dto.accounts.response;

import com.btbs.domain.accounts.AccountStatus;
import com.btbs.domain.accounts.AccountType;

import java.math.BigDecimal;
import java.util.UUID;

public record AccountDetailsResponse(
        UUID id,
        UUID customerId,
        String accountNumber,
        String currency,
        AccountType accType,
        AccountStatus status,
        BigDecimal balance
) {
}
