package com.btbs.api.web.dto.accounts.response;

import java.util.UUID;

public record AccountResponse(
        UUID id,
        String accountNumber
) { }
