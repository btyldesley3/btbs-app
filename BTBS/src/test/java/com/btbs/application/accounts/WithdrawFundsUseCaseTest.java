package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.WithdrawFundsCommand;
import com.btbs.domain.accounts.*;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class WithdrawFundsUseCaseTest {

    @Test
    void withdraws_When_Policy_Allows() {
        var accountRepository = mock(AccountRepository.class);
        var currency = Currency.getInstance("GBP");
        var accountId = new AccountId(UUID.randomUUID());

        var account = new CustomerAccount(
                accountId,
                new CustomerId(UUID.randomUUID()),
                new AccountNumber("GB00BTBS000000000021"),
                currency,
                AccountType.CURRENT,
                AccountStatus.ACTIVE,
                Money.of(new BigDecimal("100.00"), currency),
                new OverdraftPolicy(true, new BigDecimal("50.00")),
                0L
        );
        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        var useCase = new WithdrawFundsUseCase(accountRepository);
        useCase.withdrawFunds(new WithdrawFundsCommand(accountId.value(), new BigDecimal("25.00"), currency));

        verify(accountRepository).save(argThat(updated ->
                updated.balance().amount().compareTo(new BigDecimal("75.00")) == 0));
    }

    @Test
    void throws_When_Account_Missing() {
        var accountRepository = mock(AccountRepository.class);
        var withdrawFundsUseCase = new WithdrawFundsUseCase(accountRepository);

        assertThrows(IllegalArgumentException.class, () ->
                withdrawFundsUseCase.withdrawFunds(new WithdrawFundsCommand(
                        UUID.randomUUID(), new BigDecimal("1.00"), Currency.getInstance("GBP"))));
    }
}

