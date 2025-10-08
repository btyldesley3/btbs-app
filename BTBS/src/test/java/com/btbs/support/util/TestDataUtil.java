package com.btbs.support.util;

import com.btbs.domain.accounts.*;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

public class TestDataUtil {

    public static CustomerAccount newActiveCustomerAccount(BigDecimal balance, boolean overdraft, BigDecimal limit) {
        var id = new AccountId(UUID.randomUUID());
        var custId = new CustomerId(UUID.randomUUID());
        var accNo = new AccountNumber("GB00BTBS000000000001");
        var ccy = Currency.getInstance("GBP");
        return new CustomerAccount(
                id, custId, accNo, ccy,
                AccountType.CURRENT, AccountStatus.ACTIVE,
                Money.of(balance, ccy),
                new OverdraftPolicy(overdraft, limit),
                0L
        );
    }
}
