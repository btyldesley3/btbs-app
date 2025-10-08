package com.btbs.domain.accounts;

public final class AccountNumber {

    private final String value; //Validated elsewhere

    public AccountNumber(String value) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException("account number blank");
        this.value = value;
    }

    public String value(){
        return value;
    }

    @Override
    public String toString(){
        return value;
    }

    @Override
    public boolean equals(Object o){
        return o instanceof AccountNumber a && value.equals(a.value);
    }

    @Override
    public int hashCode(){
        return value.hashCode();
    }

}
