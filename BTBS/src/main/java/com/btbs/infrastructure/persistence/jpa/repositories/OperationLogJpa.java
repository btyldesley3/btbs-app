package com.btbs.infrastructure.persistence.jpa.repositories;

import com.btbs.infrastructure.persistence.jpa.entities.OperationLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OperationLogJpa extends JpaRepository<OperationLogEntity, UUID> {

}
