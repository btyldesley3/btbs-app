package com.btbs.application.accounts.dto;

import java.util.UUID;

public record GetAccountBalanceQuery(
        UUID accountId
) {
}
