package com.btbs.domain.accounts;

import com.btbs.domain.shared.value.Money;

import java.math.BigDecimal;
import java.util.Objects;

public final class OverdraftPolicy {

    private final boolean enabled;

    private final BigDecimal limitAbs;

    public OverdraftPolicy(boolean enabled, BigDecimal limitAbs) {
        this.enabled = enabled;
        this.limitAbs = enabled ? Objects.requireNonNull(limitAbs) : BigDecimal.ZERO;
        if (this.limitAbs.signum() < 0) throw new IllegalArgumentException("overdraft limit < 0");
    }

    public boolean allows(Money balance) {
        return enabled ? balance.amount().compareTo(limitAbs.negate()) >= 0 : balance.amount().signum() >= 0;
    }

    public boolean enabled(){
        return enabled;
    }

    public BigDecimal limitAbs(){
        return limitAbs;
    }
}
