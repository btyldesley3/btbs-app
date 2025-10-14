package com.btbs.api.web.advice;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.net.BindException;
import java.time.Instant;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    //Advice class for exception handling specific to RestController classes
    //Idea to assign common exceptions seen with RestApi's of this type to a map
    //which then provides details of exception to terminal

    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            BindException.class,
            IllegalArgumentException.class,
            MethodArgumentTypeMismatchException.class,
            HttpMessageNotReadableException.class // ← add this
    })
    public ResponseEntity<?> badRequest(Exception ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(problem("bad_request", safeMessage(ex), 400));
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    public ResponseEntity<?> conflict(OptimisticLockingFailureException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(problem("conflict", "Concurrent update detected", 409));
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public Map<String, Object> generic(Exception ex) {
        return problem("internal_error", "Unexpected error", 500);
    }

    private Map<String, Object> problem(String code, String detail, int status) {
        return Map.of("type",
                "about:blank",
                "title",
                code,
                "status",
                status,
                "detail",
                detail,
                "timestamp",
                java.time.Instant.now().toString());
    }

    private String safeMessage(Exception ex) {
        // Keep responses tidy; you can log full details separately
        String msg = ex.getMessage();
        return (msg == null || msg.isBlank()) ? ex.getClass().getSimpleName() : msg;
    }
}
