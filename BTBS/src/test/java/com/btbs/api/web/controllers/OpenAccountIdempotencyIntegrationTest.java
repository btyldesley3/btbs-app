package com.btbs.api.web.controllers;

import com.btbs.api.web.advice.GlobalExceptionHandler;

import com.btbs.api.web.dto.accounts.request.OpenAccountRequest;
import com.btbs.application.accounts.OpenAccountUseCase;
import com.btbs.application.accounts.dto.OpenAccountCommand;
import com.btbs.domain.accounts.AccountType;
import com.btbs.domain.shared.id.AccountId;
import com.btbs.infrastructure.persistence.jpa.entities.OperationLogEntity;
import com.btbs.infrastructure.persistence.jpa.repositories.OperationLogJpa;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase
@WithMockUser
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class OpenAccountIdempotencyIntegrationTest {

    @Autowired
    MockMvc mvc;
    @Autowired
    ObjectMapper om;
    @Autowired
    DataSource ds;
    @Autowired
    OperationLogJpa opRepo;

    @MockitoBean
    OpenAccountUseCase openAccountUseCase;

    @Test
    void operation_Log_Table_Exists() throws Exception {
        try (var c = ds.getConnection();
             var rs = c.getMetaData().getTables(null, null, "OPERATION_LOG", null)) {
            assertTrue(rs.next(), "operation_log table should exist");
        }
    }

    @Test
    void can_Insert_Operation_Log_Row() {
        var now = Instant.now();
        var e = new OperationLogEntity(
                UUID.randomUUID(),
                "POST /accounts",
                null,
                "0".repeat(64),
                OperationLogEntity.Status.IN_PROGRESS,
                0,
                "application/json",
                "",               // NEVER null
                now,
                now.plus(java.time.Duration.ofHours(24))
        );
        opRepo.save(e); // if this throws, the stacktrace will show EXACT cause
    }

    @Test
    void sameOperationId_SameBody_Returns200_On_Replay_And_Calls_UseCase_once() throws Exception {
        // Arrange
        UUID opId = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        when(openAccountUseCase.openAccount(any(OpenAccountCommand.class)))
                .thenReturn(new AccountId(createdId));

        OpenAccountRequest body = new OpenAccountRequest(
                opId,
                UUID.randomUUID(),                           // customerId
                "GB00BTBS000000000777",                      // accountNumber
                "GBP",                                       // currency (string in API DTO)
                AccountType.CURRENT,
                new BigDecimal("10.00"),
                true,
                new BigDecimal("5.00")
        );
        String json = om.writeValueAsString(body);

        // First call -> executes, returns 201 Created
        mvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andDo(print())
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/accounts/" + createdId))
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.accountNumber").value("GB00BTBS000000000777"));

        // Second call with the same operationId + same body -> replay with 200 OK
        mvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(createdId.toString()))
                .andExpect(jsonPath("$.accountNumber").value("GB00BTBS000000000777"));

        // Use case should have run only once
        verify(openAccountUseCase, times(1)).openAccount(any(OpenAccountCommand.class));
    }

    @Test
    void sameOperationId_DifferentBody_Returns409_Conflict() throws Exception {
        // Arrange
        UUID opId = UUID.randomUUID();
        UUID createdId = UUID.randomUUID();
        when(openAccountUseCase.openAccount(any(OpenAccountCommand.class)))
                .thenReturn(new AccountId(createdId));

        // First body
        OpenAccountRequest body1 = new OpenAccountRequest(
                opId,
                UUID.randomUUID(),
                "GB00BTBS000000000001",
                "GBP",
                AccountType.CURRENT,
                new BigDecimal("0.00"),
                false,
                null
        );
        // Second body differs (e.g., accountNumber)
        OpenAccountRequest body2 = new OpenAccountRequest(
                opId,                                        // same operationId
                UUID.randomUUID(),
                "GB00BTBS000000000002",                      // different
                "GBP",
                AccountType.CURRENT,
                new BigDecimal("0.00"),
                false,
                null
        );

        // First call succeeds (201) and stores snapshot
        mvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body1)))
                .andDo(print())
                .andExpect(status().isCreated());

        // Second call with same operationId but different body -> 409 Conflict
        mvc.perform(post("/accounts")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(om.writeValueAsString(body2)))
                .andExpect(status().isConflict());
    }
}
