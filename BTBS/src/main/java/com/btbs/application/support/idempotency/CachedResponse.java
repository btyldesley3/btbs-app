package com.btbs.application.support.idempotency;

public record CachedResponse(
        int httpStatus,
        String contentType,
        String responseBody) { }
