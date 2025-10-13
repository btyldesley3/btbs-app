package com.btbs.application.customers.dto;

import java.time.LocalDate;

public record CreateCustomerCommand(
        String fullName,
        LocalDate dateOfBirth,
        String email,
        String phoneNumber,
        boolean marketingOptIn
) { }
