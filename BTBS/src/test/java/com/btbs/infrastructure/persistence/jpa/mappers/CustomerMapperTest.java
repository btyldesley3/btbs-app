package com.btbs.infrastructure.persistence.jpa.mappers;

import com.btbs.domain.customers.Customer;
import com.btbs.domain.customers.KycStatus;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.PhoneNumber;
import com.btbs.infrastructure.persistence.jpa.entities.CustomerEntity;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CustomerMapperTest {

    @Test
    void domain_To_Entity_And_Back() {
        var domain = new Customer(
                new CustomerId(UUID.randomUUID()),
                "Jane Doe",
                LocalDate.of(1990, 2, 2),
                "jane@btbs.com",
                PhoneNumber.of("+447911123456"),
                KycStatus.VERIFIED,
                false,
                0L
        );

        CustomerEntity entity = CustomerMapper.toEntity(domain);
        Customer back = CustomerMapper.toDomain(entity);

        assertEquals(domain.id().value(), entity.getId());
//        System.out.println(domain.id().value());
//        System.out.println(entity.getId());
        assertEquals(domain.phone().value(), entity.getPhoneE164(), back.phone().value());
//        System.out.println(entity.getPhoneE164());
        assertEquals(domain.email(), entity.getEmail(), back.email());
//        System.out.println(back.email());
        assertEquals(domain.kycStatus().toString(), entity.getKycStatus().toString(), back.kycStatus().toString());
//        System.out.println(back.kycStatus().toString());
    }
}
