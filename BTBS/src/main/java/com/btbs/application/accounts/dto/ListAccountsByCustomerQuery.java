package com.btbs.application.accounts.dto;

import java.util.UUID;

public record ListAccountsByCustomerQuery(
        UUID customerId,
        int page,
        int size
) {
}
