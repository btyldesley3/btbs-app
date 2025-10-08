package com.btbs.infrastructure.persistence.jpa.mappers;

import com.btbs.domain.customers.Customer;
import com.btbs.domain.customers.KycStatus;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.PhoneNumber;
import com.btbs.infrastructure.persistence.jpa.entities.CustomerEntity;

import java.util.UUID;

public final class CustomerMapper {

    private CustomerMapper() {}

    public static CustomerEntity toEntity(Customer domain) {
        if (domain == null) return null;
        return new CustomerEntity(
                domain.id().value(),
                domain.fullName(),
                domain.dateOfBirth(),
                domain.email(),
                domain.phone().value(),       // E.164 string
                domain.kycStatus(),
                domain.marketingOptIn()
        );
    }

    public static Customer toDomain(CustomerEntity entity) {
        if (entity == null) return null;
        return new Customer(
                new CustomerId(entity.getId()),
                entity.getFullName(),
                entity.getDateOfBirth(),
                entity.getEmail(),
                PhoneNumber.of(entity.getPhoneE164()),
                entity.getKycStatus() == null ? KycStatus.PENDING : entity.getKycStatus(),
                entity.isMarketingOptIn()
        );
    }

    // convenience creators
    public static CustomerId toCustomerId(UUID id) {
        return id == null ? null : new CustomerId(id);
    }

}
