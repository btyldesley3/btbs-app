package com.btbs.domain.shared.id;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public final class AccountId implements Serializable {

    private final UUID value;

    public AccountId(UUID value) {
        this.value = Objects.requireNonNull(value);
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }

    public UUID value() {
        return value;
    }

    @Override
    public boolean equals(Object o){
        return o instanceof AccountId a && value.equals(a.value);
    }

    @Override
    public int hashCode(){
        return value.hashCode();
    }

    @Override
    public String toString(){
        return value.toString();
    }
}
