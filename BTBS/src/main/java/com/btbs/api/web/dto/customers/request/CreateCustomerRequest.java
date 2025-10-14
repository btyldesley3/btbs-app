package com.btbs.api.web.dto.customers.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record CreateCustomerRequest(
        @NotBlank @Size(max = 200) String fullName,
        @NotNull LocalDate dateOfBirth,
        @Email @NotBlank String email,
        @NotBlank String phoneNumber,
        boolean marketingOptIn

) {}
