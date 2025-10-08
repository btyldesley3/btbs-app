package com.btbs.infrastructure.persistence.jpa.repositories;

import com.btbs.infrastructure.persistence.jpa.entities.CustomerEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataCustomerJpa extends JpaRepository<CustomerEntity, UUID> {

    Optional<CustomerEntity> findByEmail(String email);

    boolean existsByEmail(String email);
}
