package com.btbs.api.web.controllers;

import com.btbs.api.web.advice.GlobalExceptionHandler;
import com.btbs.api.web.dto.accounts.request.DepositRequest;
import com.btbs.api.web.dto.accounts.request.OpenAccountRequest;
import com.btbs.api.web.dto.accounts.request.TransferRequest;
import com.btbs.api.web.dto.accounts.request.WithdrawRequest;
import com.btbs.application.accounts.*;
import com.btbs.application.accounts.dto.*;
import com.btbs.application.support.idempotency.BeginResult;
import com.btbs.application.support.idempotency.IdempotencyService;
import com.btbs.domain.accounts.AccountType;
import com.btbs.domain.shared.id.AccountId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
@AutoConfigureMockMvc(addFilters = true) // security filters on (we're adding csrf)
@Import(GlobalExceptionHandler.class)
@WithMockUser
class AccountControllerTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;

    // Application use cases
    @MockitoBean
    OpenAccountUseCase openAccountUseCase;
    @MockitoBean
    DepositFundsUseCase depositFundsUseCase;
    @MockitoBean
    WithdrawFundsUseCase withdrawFundsUseCase;
    @MockitoBean
    TransferFundsService transferFundsService;

    // Query use cases (if your controller constructor includes them)
    @MockitoBean
    GetAccountBalanceUseCase getAccountBalanceUseCase;
    @MockitoBean
    ListAccountsByCustomerUseCase listAccountsByCustomerUseCase;

    // NEW dependency after idempotency was added
    @MockitoBean
    IdempotencyService idempotencyService;

    @Test
    void opens_Account_Returns_201_And_Location() throws Exception {
        UUID opId = UUID.randomUUID();
        UUID id = UUID.randomUUID();

        // Use case result
        when(openAccountUseCase.openAccount(any(OpenAccountCommand.class)))
                .thenReturn(new AccountId(id));

        // Idempotency stubs — CRITICAL to avoid NPE/500
        when(idempotencyService.tryGet(eq(opId))).thenReturn(Optional.empty());
        when(idempotencyService.begin(eq(opId), eq("POST /accounts"), isNull(), anyString()))
                .thenReturn(BeginResult.fresh());
//        when(idempotencyService.begin(eq(opId), startsWith("POST "), any(), anyString()))
//                .thenReturn(BeginResult.fresh()); //non hardcoded URL matcher

        // commit is void; default is fine, but you can make it explicit:
        doNothing().when(idempotencyService)
                .commit(eq(opId), eq(201), anyString(), anyString(), any());

        var req = new OpenAccountRequest(
                opId,                                  // operationId
                UUID.randomUUID(),                     // customerId
                "GB00BTBS000000000010",
                "GBP",
                AccountType.CURRENT,
                new BigDecimal("100.00"),
                true,
                new BigDecimal("50.00")
        );

        mvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/accounts/" + id))
                .andExpect(jsonPath("$.id").value(id.toString()))
                .andExpect(jsonPath("$.accountNumber").value("GB00BTBS000000000010"));
    }

    @Test
    void deposit_Returns_204() throws Exception {
        UUID accountId = UUID.randomUUID();
        var req = new DepositRequest(
                UUID.randomUUID(),                     // operationId (NEW)
                new BigDecimal("25.00"),
                "GBP"                                  // String
        );

        mvc.perform(post("/accounts/{id}/deposit", accountId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void withdraw_Returns_204() throws Exception {
        UUID accountId = UUID.randomUUID();
        var req = new WithdrawRequest(
                UUID.randomUUID(),                     // operationId (NEW)
                new BigDecimal("10.00"),
                "GBP"
        );

        mvc.perform(post("/accounts/{id}/withdraw", accountId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void transfer_Returns_202() throws Exception {
        var req = new TransferRequest(
                UUID.randomUUID(),                     // operationId (NEW)
                UUID.randomUUID(),                     // sourceAccountId
                UUID.randomUUID(),                     // destinationAccountId
                new BigDecimal("5.00"),
                "GBP"
        );

        mvc.perform(post("/transfers")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isAccepted());
    }

    @Test
    void deposit_Conflict_Translates_To_409() throws Exception {
        UUID accountId = UUID.randomUUID();
        var req = new DepositRequest(
                UUID.randomUUID(),
                new BigDecimal("5.00"),
                "GBP"
        );

        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(depositFundsUseCase).depositFunds(any(DepositFundsCommand.class));

        mvc.perform(post("/accounts/{id}/deposit", accountId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("conflict")));
    }

    @Test
    void open_Account_400_On_Validation() throws Exception {
        // Missing accountNumber & operationId provided; overdraft disabled to avoid unrelated nulls
        var invalid = """
          {
            "operationId":"%s",
            "customerId":"%s",
            "currency":"GBP",
            "type":"CURRENT",
            "openingBalance": 0.00,
            "overdraftEnabled": false
          }
        """.formatted(UUID.randomUUID(), UUID.randomUUID());

        mvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("bad_request")));
    }
}
