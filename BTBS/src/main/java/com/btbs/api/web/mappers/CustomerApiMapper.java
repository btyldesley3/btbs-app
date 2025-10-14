package com.btbs.api.web.mappers;

import com.btbs.api.web.dto.customers.request.CreateCustomerRequest;
import com.btbs.application.customers.dto.CreateCustomerCommand;

public final class CustomerApiMapper {

    private CustomerApiMapper() {}

    public static CreateCustomerCommand toCommand(CreateCustomerRequest request) {
        return new CreateCustomerCommand(request.fullName(), request.dateOfBirth(), request.email(),
                request.phoneNumber(), request.marketingOptIn()
        );
    }
}
