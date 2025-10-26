package com.btbs.api.web.dto.accounts.request;

import com.btbs.domain.accounts.AccountType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public record OpenAccountRequest(
        @NotNull UUID operationId,
        @NotNull UUID customerId,
        @NotBlank @Size(max = 34) String accountNumber,
        @NotBlank @Size(min = 3, max = 3) String currency,
        @NotNull AccountType accType,
        @NotNull @DecimalMin("0.00") BigDecimal openingBalance,
        boolean overdraftEnabled,
        @DecimalMin("0.00") BigDecimal overdraftLimit
) { }
