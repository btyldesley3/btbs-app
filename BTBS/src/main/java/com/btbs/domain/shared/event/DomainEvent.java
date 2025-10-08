package com.btbs.domain.shared.event;

import java.time.Instant;

public interface DomainEvent {
    Instant occurredOn();

    String type();
}
