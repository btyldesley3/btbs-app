package com.btbs.domain.accounts;

import com.btbs.domain.shared.id.AccountId;

import java.util.Optional;

public interface AccountRepository {

    Optional<CustomerAccount> findById(AccountId id);

    void save(CustomerAccount account);

    boolean existsByAccountNumber(AccountNumber number);
}
