package com.btbs.domain.accounts;

import com.btbs.domain.shared.id.AccountId;
import com.btbs.domain.shared.value.Money;

import java.time.Instant;
import java.util.Objects;

public final class TransferService {

    private final AccountRepository accounts;

    public TransferService(AccountRepository accounts) {
        this.accounts = Objects.requireNonNull(accounts);
    }

    public void transfer(AccountId source, AccountId target, Money amount, Instant at) {
        var src = accounts.findById(source).orElseThrow(() -> new IllegalArgumentException("source not found"));
        var dst = accounts.findById(target).orElseThrow(() -> new IllegalArgumentException("target not found"));

        var debited = src.withdraw(amount, at);
        var credited = dst.deposit(amount, at);

        // In a single app-layer transaction, persist both
        accounts.save(debited);
        accounts.save(credited);
        // Application layer can publish debited.events() / credited.events() after commit
    }
}
