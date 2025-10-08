package com.btbs.infrastructure.persistence;

import com.btbs.domain.accounts.AccountNumber;
import com.btbs.domain.accounts.AccountRepository;
import com.btbs.domain.accounts.CustomerAccount;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.infrastructure.persistence.jpa.entities.AccountEntity;
import com.btbs.infrastructure.persistence.jpa.mappers.AccountMapper;
import com.btbs.infrastructure.persistence.jpa.repositories.SpringDataAccountJpa;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional(readOnly = true)
public class JpaAccountRepository implements AccountRepository {

    private final SpringDataAccountJpa jpa;

    public JpaAccountRepository(SpringDataAccountJpa jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<CustomerAccount> findById(AccountId id) {
        return jpa.findById(id.value()).map(AccountMapper::toDomain);
    }

    @Override
    @Transactional //write
    public void save(CustomerAccount account) {
        AccountEntity entity = AccountMapper.toEntity(account);
        jpa.save(entity);
    }

    @Override
    public boolean existsByAccountNumber(AccountNumber number) {
        return jpa.existsByAccountNumber(number.value());
    }
}
