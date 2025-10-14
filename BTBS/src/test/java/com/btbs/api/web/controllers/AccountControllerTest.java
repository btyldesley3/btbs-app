package com.btbs.api.web.controllers;

import com.btbs.api.web.advice.GlobalExceptionHandler;
import com.btbs.api.web.dto.accounts.request.DepositRequest;
import com.btbs.api.web.dto.accounts.request.OpenAccountRequest;
import com.btbs.api.web.dto.accounts.request.TransferRequest;
import com.btbs.api.web.dto.accounts.request.WithdrawRequest;
import com.btbs.application.accounts.DepositFundsUseCase;
import com.btbs.application.accounts.OpenAccountUseCase;
import com.btbs.application.accounts.TransferFundsService;
import com.btbs.application.accounts.WithdrawFundsUseCase;
import com.btbs.application.accounts.dto.DepositFundsCommand;
import com.btbs.application.accounts.dto.OpenAccountCommand;
import com.btbs.domain.accounts.AccountType;
import com.btbs.domain.shared.id.AccountId;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.context.annotation.Import;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(controllers = AccountController.class)
//@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
@WithMockUser
public class AccountControllerTest {

    @Autowired
    MockMvc mvc;

    @Autowired
    ObjectMapper om;

    @MockitoBean
    OpenAccountUseCase openAccountUseCase;

    @MockitoBean
    DepositFundsUseCase depositFundsUseCase;

    @MockitoBean
    WithdrawFundsUseCase withdrawFundsUseCase;

    @MockitoBean
    TransferFundsService transferFundsService;

    @Test
    void opens_Account_Returns_201_And_Location() throws Exception {
        UUID id = UUID.randomUUID();
        when(openAccountUseCase.openAccount(any(OpenAccountCommand.class)))
                .thenReturn(new AccountId(id));

        var req = new OpenAccountRequest(
                UUID.randomUUID(),
                "GB00BTBS000000000010",
                "GBP",
                AccountType.CURRENT,
                new BigDecimal("100.00"),
                true,
                new BigDecimal("50.00")
        );

        mvc.perform(post("/accounts")
                        .with(csrf()) // ← add this to every POST/PUT/PATCH/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
//                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/accounts/" + id))
                .andExpect(jsonPath("$.id", is(id.toString())))
                .andExpect(jsonPath("$.accountNumber", is("GB00BTBS000000000010"))
                );
    }

    @Test
    void deposit_Returns_204() throws Exception {
        UUID accountId = UUID.randomUUID();
        var req = new DepositRequest(new BigDecimal("25.00"), "GBP");

        mvc.perform(post("/accounts/{id}/deposit", accountId)
                        .with(csrf()) // ← add this to every POST/PUT/PATCH/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void withdraw_Returns_204() throws Exception {
        UUID accountId = UUID.randomUUID();
        var req = new WithdrawRequest(new BigDecimal("10.00"), "GBP");

        mvc.perform(post("/accounts/{id}/withdraw", accountId)
                        .with(csrf()) // ← add this to every POST/PUT/PATCH/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isNoContent());
    }

    @Test
    void transfer_Returns_202() throws Exception {
        var req = new TransferRequest(
                UUID.randomUUID(),
                UUID.randomUUID(),
                new BigDecimal("5.00"),
                "GBP"
        );

        mvc.perform(post("/transfers")
                        .with(csrf()) // ← add this to every POST/PUT/PATCH/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isAccepted());
    }

    @Test
    void deposit_Conflict_Translates_To_409() throws Exception {
        UUID accountId = UUID.randomUUID();
        var req = new DepositRequest(new BigDecimal("5.00"), "GBP");

        doThrow(new OptimisticLockingFailureException("conflict"))
                .when(depositFundsUseCase).depositFunds(any(DepositFundsCommand.class));

        mvc.perform(post("/accounts/{id}/deposit", accountId)
                        .with(csrf()) // ← add this to every POST/PUT/PATCH/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(req)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title", is("conflict")));
    }

    @Test
    void open_Account_400_On_Validation() throws Exception {
        // missing accountNumber
        var invalid = """
          {
            "customerId":"%s",
            "currency":"GBP",
            "type":"CURRENT",
            "openingBalance": 0.00,
            "overdraftEnabled": false
          }
        """.formatted(UUID.randomUUID());

        mvc.perform(post("/accounts")
                        .with(csrf()) // ← add this to every POST/PUT/PATCH/DELETE
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalid))
//                .andDo(print())
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title", is("bad_request"))
                );
    }

}
