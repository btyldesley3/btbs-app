package com.btbs.infrastructure.persistence.jpa.entities;

import com.btbs.domain.accounts.AccountStatus;
import com.btbs.domain.accounts.AccountType;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "accounts",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_accounts_account_number", columnNames = "account_number")},
        indexes = {
                @Index(name = "ix_accounts_customer_id", columnList = "customer_id")
        })
@Getter
@Setter
public class AccountEntity {

    @Id
    @Column(name = "id", columnDefinition = "uuid", nullable = false, updatable = false)
    private UUID id;

    @Column(name = "customer_id", columnDefinition = "uuid", nullable = false)
    private UUID customerId;

    @Column(name = "account_number", nullable = false, length = 34)
    private String accountNumber; // IBAN/local format

    @Column(name = "currency", nullable = false, length = 3)
    private String currency; // ISO 4217 (e.g., "GBP")

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false, length = 40)
    private AccountType type;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private AccountStatus status;

    @Column(name = "balance_amount", nullable = false, precision = 19, scale = 4)
    private BigDecimal balanceAmount;

    @Column(name = "overdraft_enabled", nullable = false)
    private boolean overdraftEnabled;

    @Column(name = "overdraft_limit", nullable = false, precision = 19, scale = 4)
    private BigDecimal overdraftLimit;

    @Version
    @Column(name = "version", nullable = false)
    private long version;

    protected AccountEntity() {} // JPA

    public AccountEntity(UUID id, UUID customerId, String accountNumber, String currency,
                         AccountType type, AccountStatus status, BigDecimal balanceAmount,
                         boolean overdraftEnabled, BigDecimal overdraftLimit) {
        this.id = id;
        this.customerId = customerId;
        this.accountNumber = accountNumber;
        this.currency = currency;
        this.type = type;
        this.status = status;
        this.balanceAmount = balanceAmount;
        this.overdraftEnabled = overdraftEnabled;
        this.overdraftLimit = overdraftLimit;
    }

}
