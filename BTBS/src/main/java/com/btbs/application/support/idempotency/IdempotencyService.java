package com.btbs.application.support.idempotency;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

//First implementation of Idempotency, will be improved in future iterations
public interface IdempotencyService {
    //Fast path: check if response is cached for given operationId
    Optional<CachedResponse> tryGet(UUID operationId);

    //Slow path: begin new idempotent operation
    BeginResult begin(UUID operationId, String route, String actorIdOrNull, String requestHash);

    //Commit the response for given operationId - mark as SUCCEEDED
    void commit(UUID operationId, int httpStatus, String contentType, String responseBody, Duration ttl);

    //Mark the operation as FAILED
    void fail(UUID operationId); //method for diagnostics
}

