package com.btbs.api.web.mappers;


import com.btbs.api.web.dto.accounts.request.DepositRequest;
import com.btbs.api.web.dto.accounts.request.OpenAccountRequest;
import com.btbs.api.web.dto.accounts.request.TransferRequest;
import com.btbs.api.web.dto.accounts.request.WithdrawRequest;
import com.btbs.application.accounts.dto.DepositFundsCommand;
import com.btbs.application.accounts.dto.OpenAccountCommand;
import com.btbs.application.accounts.dto.TransferFundsCommand;
import com.btbs.application.accounts.dto.WithdrawFundsCommand;
import com.btbs.domain.accounts.AccountNumber;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public final class AccountApiMapper {

    private AccountApiMapper() {}

    public static OpenAccountCommand toCommand(OpenAccountRequest request) {
        return new OpenAccountCommand(
                request.customerId(),
                new AccountNumber(request.accountNumber()),
                toCurrency(request.currency()),
                request.accType(),
                request.openingBalance(),
                request.overdraftEnabled(),
                normalizedLimit(request.overdraftEnabled(), request.overdraftLimit())
        );
    }

    public static DepositFundsCommand toCommand(UUID accountId, DepositRequest request) {
        return new DepositFundsCommand(accountId, request.amount(), toCurrency(request.currency()));
    }

    public static WithdrawFundsCommand toCommand(UUID accountId, WithdrawRequest request) {
        return new WithdrawFundsCommand(accountId, request.amount(), toCurrency(request.currency()));
    }

    public static TransferFundsCommand toCommand(TransferRequest request) {
        return new TransferFundsCommand(
                request.sourceAccountId(), request.destinationAccountId(), request.amount(),
                toCurrency(request.currency())
        );
    }

    private static Currency toCurrency(String currencyCode) {
        if (currencyCode == null || currencyCode.isBlank())
            throw new IllegalArgumentException("currency is required");
        try {
            return Currency.getInstance(currencyCode);
        } catch (Exception e) {
            throw new IllegalArgumentException("invalid currency code: " + currencyCode);
        }
    }

    private static BigDecimal normalizedLimit(boolean enabled, BigDecimal limit) {
        if (!enabled) return BigDecimal.ZERO;
        return (limit == null) ? BigDecimal.ZERO : limit;
    }
}
