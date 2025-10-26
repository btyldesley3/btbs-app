package com.btbs.application.support.idempotency;

// Exception thrown when an idempotent operation conflicts with a previous operation - may move to GlobalExceptions later
public class IdempotencyConflictException extends RuntimeException {
    public IdempotencyConflictException(String message) {
        super(message);
    }
}
