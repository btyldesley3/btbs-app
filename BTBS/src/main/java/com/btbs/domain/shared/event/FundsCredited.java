package com.btbs.domain.shared.event;

import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.value.Money;

import java.time.Instant;

public final class FundsCredited implements DomainEvent {

    private final AccountId accountId;
    private final Money amount;
    private final Instant at;

    public FundsCredited(AccountId accountId, Money amount, Instant at) {
        this.accountId = accountId;
        this.amount = amount;
        this.at = at;
    }

    public AccountId accountId(){
        return accountId;
    }

    public Money amount(){
        return amount;
    }

    @Override
    public Instant occurredOn() {
        return at;
    }

    @Override
    public String type() {
        return "FundsCredited";
    }
}
