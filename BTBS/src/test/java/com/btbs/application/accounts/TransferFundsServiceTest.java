package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.TransferFundsCommand;
import com.btbs.domain.accounts.*;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import org.junit.jupiter.api.Test;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class TransferFundsServiceTest {

    @Test
    void transfer_When_Two_Accounts_Are_The_Same_Currency() {
        var accountRepository = mock(AccountRepository.class);
        var currency = Currency.getInstance("GBP");

        var sourceAccountId = new AccountId(UUID.randomUUID());
        var destinationAccountId = new AccountId(UUID.randomUUID());

        var sourceAccount = new CustomerAccount(
                sourceAccountId,
                new CustomerId(UUID.randomUUID()),
                new AccountNumber("GB00BTBS000000000031"),
                currency,
                AccountType.CURRENT,
                AccountStatus.ACTIVE,
                Money.of(new BigDecimal("100.00"), currency),
                new OverdraftPolicy(true, new BigDecimal("50.00")),
                0L
        );

        var destinationAccount = new CustomerAccount(
                destinationAccountId,
                new CustomerId(UUID.randomUUID()),
                new AccountNumber("GB00BTBS000000000032"),
                currency,
                AccountType.CURRENT,
                AccountStatus.ACTIVE,
                Money.of(new BigDecimal("10.00"), currency),
                new OverdraftPolicy(false, BigDecimal.ZERO),
                0L
        );
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(destinationAccountId)).thenReturn(Optional.of(destinationAccount));

        var transferService = new TransferFundsService(accountRepository);
        transferService.transferFunds(new TransferFundsCommand(sourceAccountId.value(),
                destinationAccountId.value(), new BigDecimal("25.00"), currency));

        verify(accountRepository).save(argThat(updated ->
                updated.id().equals(sourceAccountId) && updated.balance().amount().compareTo(
                        new BigDecimal("75.00")) == 0));
        verify(accountRepository).save(argThat(updated ->
                updated.id().equals(destinationAccountId) && updated.balance().amount().compareTo(
                        new BigDecimal("35.00")) == 0));
    }

    @Test
    void rejects_Currency_Mismatch() {
        var accountRepository = mock(AccountRepository.class);
        var gbp = Currency.getInstance("GBP");
        var eur = Currency.getInstance("EUR");

        var sourceAccountId = new AccountId(UUID.randomUUID());
        var destinationAccountId = new AccountId(UUID.randomUUID());

        var sourceAccount = new CustomerAccount(
                sourceAccountId, new CustomerId(UUID.randomUUID()),
                new AccountNumber("SRC"),
                gbp, AccountType.CURRENT, AccountStatus.ACTIVE,
                Money.of(new BigDecimal("100.00"), gbp),
                new OverdraftPolicy(true, new BigDecimal("50.00")),
                0L
        );
        var destinationAccount = new CustomerAccount(
                destinationAccountId, new CustomerId(UUID.randomUUID()),
                new AccountNumber("DST"),
                eur, AccountType.CURRENT, AccountStatus.ACTIVE,
                Money.of(new BigDecimal("10.00"), eur),
                new OverdraftPolicy(false, BigDecimal.ZERO),
                0L
        );

        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(destinationAccountId)).thenReturn(Optional.of(destinationAccount));

        var transferService = new TransferFundsService(accountRepository);
        assertThrows(IllegalArgumentException.class, () ->
                transferService.transferFunds(new TransferFundsCommand(sourceAccountId.value(), destinationAccountId.value(),
                        new BigDecimal("1.00"), gbp)));
    }

    @Test
    void retry_On_Optimistic_Lock_Then_Succeeds() {
        var accountRepository = mock(AccountRepository.class);
        var currency = Currency.getInstance("GBP");

        var sourceAccountId = new AccountId(UUID.randomUUID());
        var destinationAccountId = new AccountId(UUID.randomUUID());

        var sourceAccount = new CustomerAccount(
                sourceAccountId, new CustomerId(UUID.randomUUID()),
                new AccountNumber("SRC"),
                currency, AccountType.CURRENT, AccountStatus.ACTIVE,
                Money.of(new BigDecimal("100.00"), currency),
                new OverdraftPolicy(true, new BigDecimal("50.00")),
                0L
        );
        var destinationAccount = new CustomerAccount(
                destinationAccountId, new CustomerId(UUID.randomUUID()),
                new AccountNumber("DST"),
                currency, AccountType.CURRENT, AccountStatus.ACTIVE,
                Money.of(new BigDecimal("10.00"), currency),
                new OverdraftPolicy(false, BigDecimal.ZERO),
                0L
        );

        // On first attempt: loads OK
        when(accountRepository.findById(sourceAccountId)).thenReturn(Optional.of(sourceAccount));
        when(accountRepository.findById(destinationAccountId)).thenReturn(Optional.of(destinationAccount));
        // First save triggers locking failure (simulate on source save)
        doThrow(new OptimisticLockingFailureException("conflict"))
                .doNothing() // retry succeeds
                .when(accountRepository).save(argThat(a -> a.id().equals(sourceAccountId)));
        // Destination save always OK
        doNothing().when(accountRepository).save(argThat(a -> a.id().equals(destinationAccountId)));

        var transferService = new TransferFundsService(accountRepository);
        assertDoesNotThrow(() ->
                transferService.transferFunds(new TransferFundsCommand(sourceAccountId.value(), destinationAccountId.value(),
                        new BigDecimal("5.00"), currency)));

        // Verify at least two attempts on source
        verify(accountRepository, atLeast(2)).save(argThat(a -> a.id().equals(sourceAccountId)));
    }

}


