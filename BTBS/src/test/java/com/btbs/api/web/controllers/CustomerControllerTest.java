package com.btbs.api.web.controllers;

import com.btbs.api.web.advice.GlobalExceptionHandler;
import com.btbs.api.web.dto.customers.request.CreateCustomerRequest;
import com.btbs.application.customers.CreateCustomerUseCase;
import com.btbs.application.customers.dto.CreateCustomerCommand;
import com.btbs.domain.shared.id.CustomerId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = CustomerController.class)
@Import(GlobalExceptionHandler.class)
@WithMockUser
public class CustomerControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    CreateCustomerUseCase createCustomerUseCase;

    @Test
    void  creates_Customer_And_Returns_201_With_Location() throws Exception {
        UUID id = UUID.randomUUID();
        when(createCustomerUseCase.createCustomer(any(CreateCustomerCommand.class))).thenReturn(new CustomerId(id));

        var body = new CreateCustomerRequest(
                "Jane Doe",
                LocalDate.of(1990,1,1),
                "jane@example.com",
                "+447911123456",
                true
        );
        mvc.perform(post("/customers")
                        .with(csrf()) // ← add this to every POST/PUT/PATCH/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/customers/" + id))
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.fullName", is("Jane Doe")))
                .andExpect(jsonPath("$.email", is("jane@example.com")));

        var captor = ArgumentCaptor.forClass(CreateCustomerCommand.class);
        verify(createCustomerUseCase).createCustomer(captor.capture());
    }

    @Test
    void returns_400_On_Validation_Error() throws Exception {
        // missing required fields -> @Valid triggers GlobalExceptionHandler
        var invalid = """
          { "fullName": "", "email": "", "phoneNumber": "" }
        """;
        mvc.perform(post("/customers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("bad_request")));
    }

}
