package com.btbs.infrastructure.persistence.jpa;

import com.btbs.domain.customers.Customer;
import com.btbs.domain.customers.CustomerRepository;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.infrastructure.persistence.jpa.entities.CustomerEntity;
import com.btbs.infrastructure.persistence.jpa.mappers.CustomerMapper;
import com.btbs.infrastructure.persistence.jpa.repositories.SpringDataCustomerJpa;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaCustomerRepository implements CustomerRepository {

    private final SpringDataCustomerJpa jpa;

    public JpaCustomerRepository(SpringDataCustomerJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<Customer> findById(CustomerId id) {
        return jpa.findById(id.value()).map(CustomerMapper::toDomain);
    }

    @Override
    @Transactional //write
    public void save(Customer customer) {
        CustomerEntity entity = CustomerMapper.toEntity(customer);
        jpa.save(entity);
    }

    @Override
    public boolean existsByEmail(String email) {
        return jpa.existsByEmail(email);
    }
}
