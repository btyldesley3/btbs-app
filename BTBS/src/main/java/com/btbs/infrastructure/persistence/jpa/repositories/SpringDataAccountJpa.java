package com.btbs.infrastructure.persistence.jpa.repositories;

import com.btbs.infrastructure.persistence.jpa.entities.AccountEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataAccountJpa extends JpaRepository<AccountEntity, UUID> {

    Optional<AccountEntity> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Page<AccountEntity> findByCustomerId(UUID customerId, Pageable pageable);
}
