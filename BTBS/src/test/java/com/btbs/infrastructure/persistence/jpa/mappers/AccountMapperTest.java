package com.btbs.infrastructure.persistence.jpa.mappers;

import com.btbs.domain.accounts.*;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import com.btbs.infrastructure.persistence.jpa.entities.AccountEntity;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AccountMapperTest {

    @Test
    void Domain_To_Entity_And_Back() {
        var ccy = Currency.getInstance("GBP");
        var domain = new CustomerAccount(
                new AccountId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                new AccountNumber("GB00BTBS000000000001"),
                ccy,
                AccountType.CURRENT,
                AccountStatus.ACTIVE,
                Money.of(new BigDecimal("123.45"), ccy),
                new OverdraftPolicy(true, new BigDecimal("50.00")),
                0L
        );

        AccountEntity entity = AccountMapper.toEntity(domain);
        CustomerAccount back = AccountMapper.toDomain(entity);

        assertEquals(domain.id().toString(), entity.getId().toString(), back.id().value().toString());
//        System.out.println(domain.id()); System.out.println(entity.getId().toString()); System.out.println(back.id().value().toString());
        assertEquals(domain.currency().toString(), entity.getCurrency(), back.currency().toString());
        assertEquals(domain.balance().toString(), "GBP " + entity.getBalanceAmount(), back.balance().toString());
//        System.out.println(domain.balance()); System.out.println(entity.getBalanceAmount()); System.out.println(back.balance());
        assertEquals(domain.status().toString(), back.status().toString(), entity.getStatus().toString());
//        System.out.println(domain.status()); System.out.println(back.status()); System.out.println(entity.getStatus());
        assertEquals(domain.overdraft().limitAbs().toString(),
                back.overdraft().limitAbs().toString(), entity.getOverdraftLimit().toString());
//        System.out.println(domain.overdraft().limitAbs()); System.out.println(back.overdraft().limitAbs()); System.out.println(entity.getOverdraftLimit());
    }
}
