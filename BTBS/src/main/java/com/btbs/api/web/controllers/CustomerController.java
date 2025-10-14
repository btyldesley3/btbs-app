package com.btbs.api.web.controllers;

import com.btbs.api.web.dto.customers.request.CreateCustomerRequest;
import com.btbs.api.web.dto.customers.response.CustomerResponse;
import com.btbs.api.web.mappers.CustomerApiMapper;
import com.btbs.application.customers.CreateCustomerUseCase;
import com.btbs.domain.shared.id.CustomerId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

@RestController
@RequestMapping("/customers")
public class CustomerController {

    private final CreateCustomerUseCase createCustomer;

    public CustomerController(CreateCustomerUseCase createCustomer) {
        this.createCustomer = createCustomer;
    }

    @PostMapping
    public ResponseEntity<CustomerResponse> create(@Valid @RequestBody CreateCustomerRequest body) {
        CustomerId id = createCustomer.createCustomer(CustomerApiMapper.toCommand(body));
        var response = new CustomerResponse(id.value(), body.fullName(), body.email());
        return ResponseEntity.created(URI.create("/customers/" + id.value())).body(response);
    }
}
