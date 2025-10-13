package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.DepositFundsCommand;
import com.btbs.domain.accounts.*;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class DepositFundsUseCaseTest {
    @Test
    void deposits_Into_Existing_Account() {
        var accountRepository = mock(AccountRepository.class);
        var currency = Currency.getInstance("GBP");

        var account = new CustomerAccount(
                new AccountId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                new AccountNumber("GB00BTBS000000000011"),
                currency,
                AccountType.CURRENT,
                AccountStatus.ACTIVE,
                Money.of(new BigDecimal("10.00"), currency),
                new OverdraftPolicy(true, new BigDecimal("50.00")),
                0L
        );

        when(accountRepository.findById(account.id())).thenReturn(java.util.Optional.of(account));

        var useCase = new DepositFundsUseCase(accountRepository);
        useCase.depositFunds(new DepositFundsCommand(
                account.id().value(),
                new BigDecimal("2.50"),
                currency
        ));

        verify(accountRepository).save(argThat(updated ->
                updated.balance().amount().compareTo(new BigDecimal("12.50")) == 0));
    }

    @Test
    void throws_When_Account_Missing() {
        var repo = mock(AccountRepository.class);
        var useCase = new DepositFundsUseCase(repo);

        assertThrows(IllegalArgumentException.class, () ->
                useCase.depositFunds(new DepositFundsCommand(
                        UUID.randomUUID(), new BigDecimal("1.00"), Currency.getInstance("GBP"))));
    }
}
