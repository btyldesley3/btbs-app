package com.btbs.domain.customers;

import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.PhoneNumber;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.Objects;

public class Customer {

    private final CustomerId id;

    private final String fullName;

    private final LocalDate dateOfBirth;

    private final String email;

    private final PhoneNumber phone;

    private final KycStatus kycStatus;

    private final boolean marketingOptIn;

    private final long version;

    public Customer(CustomerId id, String fullName, LocalDate dob,
                    String email, PhoneNumber phone, KycStatus kycStatus, boolean marketingOptIn,
                    long version) {
        this.id = Objects.requireNonNull(id);
        this.fullName = requireNonBlank(fullName, "fullName");
        this.dateOfBirth = Objects.requireNonNull(dob);
        this.email = requireNonBlank(email, "email");
        this.phone = Objects.requireNonNull(phone);
        this.kycStatus = Objects.requireNonNull(kycStatus);
        this.marketingOptIn = marketingOptIn;
        this.version = version;
    }

    public Customer verifyKyc() {
        return new Customer(id, fullName, dateOfBirth, email, phone, KycStatus.VERIFIED, marketingOptIn, version);
    }

    // getters
    public CustomerId id(){
        return id;
    }
    public String fullName(){
        return fullName;
    }
    public LocalDate dateOfBirth(){
        return dateOfBirth;
    }
    public String email(){
        return email;
    }
    public PhoneNumber phone(){
        return phone;
    }
    public KycStatus kycStatus(){
        return kycStatus;
    }
    public boolean marketingOptIn(){
        return marketingOptIn;
    }
    public long version() {
        return version;
    }

    private static String requireNonBlank(String s, String field) {
        if (s == null || s.isBlank()) throw new IllegalArgumentException(field + " is blank");
        return s;
    }

}
