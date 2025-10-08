package com.btbs.domain.customers;

import com.btbs.domain.shared.id.CustomerId;

import java.util.Optional;

public interface CustomerRepository {
    Optional<Customer> findById(CustomerId id);

    void save(Customer customer);

    boolean existsByEmail(String email);
}
