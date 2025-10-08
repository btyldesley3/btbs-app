package com.btbs.domain.customers;

//Know Your Customer regulatory and compliance class (part of AML regulations)
public enum KycStatus {
    PENDING, //Customer submitted info but not yet verified
    VERIFIED, //Identity confirmed, full access granted
    REJECTED //Verification failed or fraudulent
}
