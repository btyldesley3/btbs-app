package com.btbs.application.accounts;

import com.btbs.application.accounts.dto.ListAccountsByCustomerQuery;
import com.btbs.infrastructure.persistence.jpa.entities.AccountEntity;
import com.btbs.infrastructure.persistence.jpa.repositories.SpringDataAccountJpa;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class ListAccountsByCustomerUseCase {

    private final SpringDataAccountJpa jpa; // read-model shortcut

    public ListAccountsByCustomerUseCase(SpringDataAccountJpa jpa) {
        this.jpa = jpa;
    }

    @Transactional(readOnly = true)
    public Page<AccountEntity> list(ListAccountsByCustomerQuery query) {
        return jpa.findByCustomerId(UUID.fromString(
                query.customerId().toString()), PageRequest.of(query.page(), query.size()));
    }

}
