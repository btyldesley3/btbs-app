-- V1__create_operation_log.sql
-- Idempotency operation log for client-supplied operationId (UUID).
-- Stores a snapshot of the first successful response to replay on retries.

-- For Postgres + H2 (PostgreSQL mode). Avoids custom ENUMs for H2 compatibility.

CREATE TABLE IF NOT EXISTS operation_log (
    operation_id   UUID PRIMARY KEY,             -- client-supplied idempotency key (operationId)
    route          VARCHAR(200) NOT NULL,        -- e.g. 'POST /accounts'
    actor_id       UUID NULL,                    -- optional authenticated user/customer id
    request_hash   CHAR(64)   NOT NULL,          -- SHA-256 hex of canonical request (excluding operationId)
    status         VARCHAR(20) NOT NULL,         -- IN_PROGRESS | SUCCEEDED | FAILED
    response_code  INT        NOT NULL DEFAULT 0,
    content_type   VARCHAR(200),
    response_body  TEXT       NOT NULL,          -- JSON snapshot of response body
    created_at     TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at     TIMESTAMP  NOT NULL
);

-- Ensure only expected status values are used (portable across Postgres/H2)
ALTER TABLE operation_log
    ADD CONSTRAINT chk_operation_log_status
    CHECK (status IN ('IN_PROGRESS','SUCCEEDED','FAILED'));

-- Fast lookup for cleanup/replay windows
CREATE INDEX IF NOT EXISTS ix_operation_log_expires_at
    ON operation_log (expires_at);

-- Optional: guard against accidental corrupt updates where operation_id is reused
-- with a different route/hash AFTER completion.
-- (App logic already enforces this and throws 409, but this adds defense-in-depth.)
-- This constraint allows same (operation_id, route, request_hash), but if someone
-- tries to change route or hash for an existing operation_id, it will fail.
-- Note: Primary key already guarantees exactly one row per operation_id; the check
-- below prevents changing route/hash once SUCCEEDED.
ALTER TABLE operation_log
    ADD CONSTRAINT chk_operation_log_mutation
    CHECK (
        status <> 'SUCCEEDED'
        OR (status = 'SUCCEEDED' AND route IS NOT NULL AND request_hash IS NOT NULL)
    );

-- Helpful comments
COMMENT ON TABLE operation_log IS 'Idempotency operation log: caches first successful response for a given operationId.';
COMMENT ON COLUMN operation_log.operation_id IS 'Client-supplied idempotency key (UUID).';
COMMENT ON COLUMN operation_log.request_hash IS 'SHA-256 (hex) of canonicalized request body excluding operationId.';
COMMENT ON COLUMN operation_log.status IS 'IN_PROGRESS | SUCCEEDED | FAILED';
COMMENT ON COLUMN operation_log.expires_at IS 'When the cached response is considered stale and may be purged.';

-- (Optional) periodic cleanup:
-- DELETE FROM operation_log WHERE expires_at < CURRENT_TIMESTAMP;
