package com.btbs.infrastructure.persistence.jpa.repositories;

import com.btbs.domain.accounts.*;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;
import com.btbs.infrastructure.persistence.JpaAccountRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Currency;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DataJpaTest
@Import({JpaAccountRepository.class})
public class JpaAccountRepositoryIntegrationTest {

    @Autowired
    private AccountRepository repo;

    @PersistenceContext
    private EntityManager em;

    @Test
    void Save_Find_And_Update_Balance() {
        var ccy = Currency.getInstance("GBP");
        var acc = new CustomerAccount(
                new AccountId(UUID.randomUUID()),
                new CustomerId(UUID.randomUUID()),
                new AccountNumber("GB00BTBS000000000002"),
                ccy,
                AccountType.CURRENT,
                AccountStatus.ACTIVE,
                Money.of(new BigDecimal("100.00"), ccy),
                new OverdraftPolicy(true, new BigDecimal("50.00")),
                0L
        );

        repo.save(acc);
        var loaded = repo.findById(acc.id()).orElseThrow();

        var updated = loaded.deposit(Money.of(new BigDecimal("25.00"), ccy), Instant.now());
        repo.save(updated);

        var reloaded = repo.findById(acc.id()).orElseThrow();
        assertEquals(new BigDecimal("125.00"), reloaded.balance().amount());
    }

    // Bane of my life exception test!
    @Test
    void Optimistic_Locking_Detects_Conflicts() {
        var ccy = Currency.getInstance("GBP");
        var id = new AccountId(UUID.randomUUID());

        var base = new CustomerAccount(
                id, new CustomerId(UUID.randomUUID()),
                new AccountNumber("GB00BTBS000000000003"),
                ccy, AccountType.CURRENT, AccountStatus.ACTIVE,
                Money.of(new BigDecimal("50.00"), ccy),
                new OverdraftPolicy(false, BigDecimal.ZERO),
                0L // domain carries version
        );
        repo.save(base);

        // two "readers" take copies (both version = 0)
        var a1 = repo.findById(id).orElseThrow();
        var a2 = repo.findById(id).orElseThrow();

        // 1) First writer updates -> DB bumps version to 1
        repo.save(a1.deposit(Money.of(new BigDecimal("10.00"), ccy), Instant.now()));
        em.flush();
        em.clear();

        // 2) Second writer still holds stale version = 0
        assertThrows(OptimisticLockingFailureException.class, () -> {
            repo.save(a2.withdraw(Money.of(new BigDecimal("5.00"), ccy), Instant.now()));
            em.flush(); // triggers the UPDATE ... WHERE id=? AND version=0 (0 rows -> exception)
        });
    }

}
