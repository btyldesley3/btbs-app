package com.btbs.infrastructure.persistence.jpa.entities;

import jakarta.persistence.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "operation_log",
        indexes = {
                @Index(name = "ix_operation_log_expires_at", columnList = "expires_at")
        })
public class OperationLogEntity {
    public enum Status {
        IN_PROGRESS, SUCCEEDED, FAILED
    }

    @Id
    @Column(name = "operation_id", nullable = false, updatable = false)
    private UUID operationId;

    @Column(name = "route", nullable = false, length = 200)
    private String route;

    @Column(name = "actor_id")
    private UUID actorId;

    @Column(name = "request_hash", nullable = false, length = 64)
    private String requestHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private Status status;

    @Column(name = "response_code", nullable = false)
    private int responseCode;

    @Column(name = "content_type", nullable = false)
    private String contentType;

    @Column(name = "response_body", columnDefinition = "TEXT", nullable = false)
    private String responseBody;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "expires_at", nullable = false)
    private Instant expiresAt;

    public OperationLogEntity() {}

    public OperationLogEntity(UUID operationId, String route, UUID actorId, String requestHash, Status status,
                                 int responseCode, String contentType, String responseBody,
                                 Instant createdAt, Instant expiresAt) {
        this.operationId = operationId;
        this.route = route;
        this.actorId = actorId;
        this.requestHash = requestHash;
        this.status = status;
        this.responseCode = responseCode;
        this.contentType = contentType;
        this.responseBody = responseBody;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
    }

    // getters & setters
    public UUID getOperationId() {
        return operationId;
    }

    public String getRoute() {
        return route;
    }

    public UUID getActorId() {
        return actorId;
    }

    public String getRequestHash() {
        return requestHash;
    }

    public Status getStatus() {
        return status;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public String getContentType() {
        return contentType;
    }

    public String getResponseBody() {
        return responseBody;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getExpiresAt() {
        return expiresAt;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public void setResponseBody(String responseBody) {
        this.responseBody = responseBody;
    }

    public void setExpiresAt(Instant expiresAt) {
        this.expiresAt = expiresAt;
    }
}
