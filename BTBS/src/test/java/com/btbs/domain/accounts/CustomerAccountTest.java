package com.btbs.domain.accounts;

import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static com.btbs.support.util.TestDataUtil.newActiveCustomerAccount;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CustomerAccountTest {

    @Test
    void deposit_Increases_Balance_And_Raises_Event() {
        var acc = newActiveCustomerAccount(new BigDecimal("10.00"), false, BigDecimal.ZERO);
        var updated = acc.deposit(Money.of(new BigDecimal("5.00"),
                Currency.getInstance("GBP")), Instant.now());
        assertEquals(new BigDecimal("15.00"), updated.balance().amount());
//        System.out.println(updated.balance().amount());
        assertEquals(1, updated.events().size());
//        System.out.println(updated.events().size());
    }

    @Test
    void withdraw_Respects_Overdraft_Policy() {
        var acc = newActiveCustomerAccount(new BigDecimal("10.00"), true, new BigDecimal("25.00"));
        var updated = acc.withdraw(Money.of(new BigDecimal("30.00"),
                Currency.getInstance("GBP")), Instant.now());
        assertEquals(new BigDecimal("-20.00"), updated.balance().amount());
//        System.out.println(updated.balance().amount());
    }

    @Test
    void withdraw_Throws_When_Limit_Exceeded() {
        var acc = newActiveCustomerAccount(new BigDecimal("10.00"), true, new BigDecimal("10.00"));
        assertThrows(IllegalStateException.class, () ->
                acc.withdraw(Money.of(new BigDecimal("25.00"), Currency.getInstance("GBP")),
                        Instant.now()));
//        System.out.println(acc.withdraw(Money.of(new BigDecimal("25.00"), Currency.getInstance("GBP")),
//          Instant.now()));
    }

    @Test
    void rejects_Currency_Mismatch() {
        var acc = newActiveCustomerAccount(new BigDecimal("10.00"), false, BigDecimal.ZERO);
        assertThrows(IllegalArgumentException.class, () ->
                acc.deposit(Money.of(new BigDecimal("1.00"), Currency.getInstance("EUR")), Instant.now()));
//        System.out.println(acc.deposit(Money.of(new BigDecimal("1.00"), Currency.getInstance("EUR")),
//                Instant.now()));
    }
}
