package com.btbs.infrastructure.persistence.jpa.entities;

import com.btbs.domain.customers.KycStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customers_email", columnNames = "email")},
        indexes = {
        @Index(name = "ix_customers_email", columnList = "email")
})
@Getter
@Setter
public class CustomerEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "full_name", nullable = false, length = 200)
    private String fullName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Column(name = "email", nullable = false, length = 320)
    private String email;

    // Stored in E.164; conversion handled by PhoneNumberConverter (autoApply = true)
    @Column(name = "phone_e164", nullable = false, length = 32)
    private String phoneE164;

    @Enumerated(EnumType.STRING)
    @Column(name = "kyc_status", nullable = false, length = 20)
    private KycStatus kycStatus;

    @Column(name = "marketing_opt_in", nullable = false)
    private boolean marketingOptIn;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected CustomerEntity() {} // JPA

    public CustomerEntity(UUID id, String fullName, LocalDate dateOfBirth, String email,
                          String phoneE164, KycStatus kycStatus, boolean marketingOptIn) {
        this.id = id;
        this.fullName = fullName;
        this.dateOfBirth = dateOfBirth;
        this.email = email;
        this.phoneE164 = phoneE164;
        this.kycStatus = kycStatus;
        this.marketingOptIn = marketingOptIn;
    }
}
