package com.btbs.application.support.idempotency;

import com.btbs.infrastructure.persistence.jpa.entities.OperationLogEntity;
import com.btbs.infrastructure.persistence.jpa.entities.OperationLogEntity.Status;
import com.btbs.infrastructure.persistence.jpa.repositories.OperationLogJpa;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
public class IdempotencyServiceImpl implements IdempotencyService {

    private final OperationLogJpa repo;

    public IdempotencyServiceImpl(OperationLogJpa repo) {
        this.repo = repo;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<CachedResponse> tryGet(UUID operationId) {
        return repo.findById(operationId)
                .filter(e -> e.getExpiresAt().isAfter(Instant.now()))
                .filter(e -> e.getStatus() == Status.SUCCEEDED)
                .map(e -> new CachedResponse(e.getResponseCode(), e.getContentType(),
                        e.getResponseBody()));
    }

    @Override
    @Transactional
    public BeginResult begin(UUID operationId, String route, String actorIdOrNull, String requestHash) {
        var existing = repo.findById(operationId).orElse(null);
        if (existing == null) {
            //create IN_PROGRESS entry
            var now = Instant.now();
            var e = new OperationLogEntity(
                    operationId,
                    route,
                    parseUuidOrNull(actorIdOrNull),
                    requestHash,
                    Status.IN_PROGRESS,
                    0,
                    null,
                    "", //response body filled on commit
                    now,
                    now.plus(Duration.ofHours(24)) //TTL 24 hours
            );
            repo.save(e);
            return BeginResult.fresh();
        }

        //if expired, treat as fresh by replacing
        if (existing.getExpiresAt().isBefore(Instant.now())) {
            existing.setStatus(Status.IN_PROGRESS);
            existing.setExpiresAt(Instant.now().plus(Duration.ofHours(24)));
            repo.save(existing);
            return BeginResult.fresh();
        }

        //same operationId used before
        if (!existing.getRoute().equals(route) || !existing.getRequestHash().equals(requestHash)) {
            throw new IdempotencyConflictException("Same operationId used with different request");
        }

        if (existing.getStatus() == Status.SUCCEEDED) {
            var cached = new CachedResponse(existing.getResponseCode(), existing.getContentType(),
                    existing.getResponseBody());
            return BeginResult.completed(cached);
        }
        //IN_PROGRESS or FAILED with same hash -> allow retry; caller will run command again and commit
        return BeginResult.fresh();
    }

    @Override
    @Transactional
    public void commit(UUID operationId, int httpStatus, String contentType, String responseBody, Duration ttl) {
        var e = repo.findById(operationId).orElseThrow();
        e.setStatus(Status.SUCCEEDED);
        e.setResponseCode(httpStatus);
        e.setContentType(contentType);
        e.setResponseBody(responseBody == null ? "" : responseBody);
        e.setExpiresAt(Instant.now().plus(ttl));
        repo.save(e);
    }

    @Override
    @Transactional
    public void fail(UUID operationId) {
        repo.findById(operationId).ifPresent(e -> {
            e.setStatus(Status.FAILED);
            repo.save(e);
        });
    }

    //helper method to parse actorId, may expand
    private static UUID parseUuidOrNull(String s) {
        if (s == null || s.isBlank()) return null;
        try {
            return UUID.fromString(s);
        } catch (IllegalArgumentException ex) {
            return null; // ignore bad actor ids
        }
    }

}
