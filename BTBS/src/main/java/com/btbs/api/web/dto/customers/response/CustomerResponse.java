package com.btbs.api.web.dto.customers.response;

import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String fullName,
        String email
) { }
