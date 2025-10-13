package com.btbs.application.customers;

import com.btbs.application.customers.dto.CreateCustomerCommand;
import com.btbs.domain.customers.Customer;
import com.btbs.domain.customers.CustomerRepository;
import com.btbs.domain.customers.KycStatus;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.PhoneNumber;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateCustomerUseCase {

    private final CustomerRepository customerRepository;

    public CreateCustomerUseCase(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    @Transactional  //May delegate exceptions to a GlobalExceptionHandler once custom exceptions created
    public CustomerId createCustomer(CreateCustomerCommand cmd) {
        if (cmd.fullName() == null || cmd.fullName().isBlank()) {
            throw new IllegalArgumentException("fullName is required");
        }
        if (cmd.email() == null || cmd.email().isBlank()) {
            throw new IllegalArgumentException("email is required");
        }
        if (customerRepository.existsByEmail(cmd.email())) {
            throw new IllegalArgumentException("Email already in use");
        }

        var customer = new Customer(
                CustomerId.newId(),
                cmd.fullName(),
                cmd.dateOfBirth(),
                cmd.email(),
                PhoneNumber.of(cmd.phoneNumber()),
                KycStatus.PENDING,
                cmd.marketingOptIn(),
                0L
                );
        customerRepository.save(customer);
        return customer.id();
    }
}
