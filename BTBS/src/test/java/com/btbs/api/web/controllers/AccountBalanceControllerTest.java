package com.btbs.api.web.controllers;


import com.btbs.api.web.advice.GlobalExceptionHandler;
import com.btbs.application.accounts.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
@Import(GlobalExceptionHandler.class)
class AccountBalanceControllerTest {
    @Autowired
    MockMvc mvc;

    @MockitoBean
    OpenAccountUseCase openAcc;

    @MockitoBean
    DepositFundsUseCase depositFunds;

    @MockitoBean
    WithdrawFundsUseCase withdrawFunds;

    @MockitoBean
    TransferFundsService transferFunds;

    @MockitoBean
    GetAccountBalanceUseCase getBalance;

    @MockitoBean
    ListAccountsByCustomerUseCase listAccByCustomer;

    @Test
    void returns_Balance() throws Exception {
        var gbp = java.util.Currency.getInstance("GBP");
        when(getBalance.getAccountBalance(any()))
                .thenReturn(com.btbs.domain.shared.value.Money.of(new java.math.BigDecimal("12.34"), gbp));

        mvc.perform(get("/accounts/{id}/balance", java.util.UUID.randomUUID()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.currency").value("GBP"))
                .andExpect(jsonPath("$.balance").value(12.34));
    }

}
