package com.btbs.infrastructure.persistence.jpa.repositories;

import com.btbs.domain.customers.Customer;
import com.btbs.domain.customers.CustomerRepository;
import com.btbs.domain.customers.KycStatus;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.PhoneNumber;
import com.btbs.infrastructure.persistence.jpa.JpaCustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DataJpaTest
@Import({JpaCustomerRepository.class})
public class JpaCustomerRepositoryIntegrationTest {

    @Autowired
    private SpringDataCustomerJpa springData; // proves SD repo is wired

    @Autowired
    private CustomerRepository repo;          // domain port implemented by our adapter

    @Test
    void Save_And_Find_Customer() {
        var id = new CustomerId(UUID.randomUUID());
        var customer = new Customer(
                id,
                "Jane Tester",
                LocalDate.of(1990, 1, 1),
                "jane@test.com",
                PhoneNumber.of("+447911123456"),
                KycStatus.PENDING,
                true,
                0L
        );

        repo.save(customer);

        var loaded = repo.findById(id).orElseThrow();
        assertEquals("Jane Tester", loaded.fullName());
        assertEquals(KycStatus.PENDING, loaded.kycStatus());
        assertTrue(springData.existsByEmail("jane@test.com"));
    }
}
