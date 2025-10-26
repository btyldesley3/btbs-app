package com.btbs.application.support.idempotency;

public record BeginResult(
        boolean alreadyCompleted,
        CachedResponse cached) {

    public static BeginResult fresh() {
        return new BeginResult(false, null);
    }

    public static BeginResult completed(CachedResponse cached) {
        return new BeginResult(true, cached);
    }
}
