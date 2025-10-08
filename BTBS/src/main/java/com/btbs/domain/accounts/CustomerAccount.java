package com.btbs.domain.accounts;

import com.btbs.domain.shared.event.DomainEvent;
import com.btbs.domain.shared.event.FundsCredited;
import com.btbs.domain.shared.event.FundsDebited;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.id.CustomerId;
import com.btbs.domain.shared.value.Money;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Currency;
import java.util.List;
import java.util.Objects;

public class CustomerAccount {

    private final AccountId id;

    private final CustomerId customerId;

    private final AccountNumber accountNumber;

    private final Currency currency;

    private final AccountType type;

    private final AccountStatus status;

    private final Money balance;

    private final OverdraftPolicy overdraft;

    private final long version;

    // Optional: collect events raised during state changes (plan to be consumed by application layer)
    private final transient List<DomainEvent> events = new ArrayList<>();

    public CustomerAccount(AccountId id,
                           CustomerId customerId,
                           AccountNumber accountNumber,
                           Currency currency,
                           AccountType type,
                           AccountStatus status,
                           Money balance,
                           OverdraftPolicy overdraft,
                           long version) {
        this.id = Objects.requireNonNull(id);
        this.customerId = Objects.requireNonNull(customerId);
        this.accountNumber = Objects.requireNonNull(accountNumber);
        this.currency = Objects.requireNonNull(currency);
        this.type = Objects.requireNonNull(type);
        this.status = Objects.requireNonNull(status);
        this.balance = Objects.requireNonNull(balance);
        this.overdraft = Objects.requireNonNull(overdraft);
        this.version = version;
        enforceInvariants();
    }

    public CustomerAccount deposit(Money amount, Instant at) {
        requireActive();
        requireSameCurrency(amount);
        CustomerAccount updated = copyWith(balance.add(amount));
        updated.events.add(new FundsCredited(id, amount, at));
        return updated;
    }

    public CustomerAccount withdraw(Money amount, Instant at) {
        requireActive();
        requireSameCurrency(amount);
        Money newBalance = balance.subtract(amount);
        if (!overdraft.allows(newBalance)) {
            throw new IllegalStateException("Insufficient funds / overdraft limit exceeded");
        }
        CustomerAccount updated = copyWith(newBalance);
        updated.events.add(new FundsDebited(id, amount, at));
        return updated;
    }

    // --- internal guards & helpers ---
    private void enforceInvariants() {
        if (!overdraft.allows(balance)) throw new IllegalStateException("Initial balance violates overdraft policy");
    }
    private void requireActive() {
        if (status != AccountStatus.ACTIVE) throw new IllegalStateException("Account not active");
    }
    private void requireSameCurrency(Money amount) {
        if (!amount.currency().equals(currency)) throw new IllegalArgumentException("Currency mismatch");
    }
    private CustomerAccount copyWith(Money newBalance) {
        return new CustomerAccount(id, customerId, accountNumber, currency, type, status, newBalance, overdraft, version);
    }

    // --- getters (for mappers / read models) ---
    public AccountId id(){
        return id;
    }
    public CustomerId customerId(){
        return customerId;
    }
    public AccountNumber accountNumber(){
        return accountNumber;
    }
    public Currency currency(){
        return currency;
    }
    public AccountType type(){
        return type;
    }
    public AccountStatus status(){
        return status;
    }
    public Money balance(){
        return balance;
    }
    public OverdraftPolicy overdraft(){
        return overdraft;
    }
    public List<DomainEvent> events(){
        return List.copyOf(events);
    }
    public long version() {
        return version;
    }

}
