package com.btbs.api.web.controllers;

import com.btbs.api.web.dto.accounts.request.DepositRequest;
import com.btbs.api.web.dto.accounts.request.OpenAccountRequest;
import com.btbs.api.web.dto.accounts.request.TransferRequest;
import com.btbs.api.web.dto.accounts.request.WithdrawRequest;
import com.btbs.api.web.dto.accounts.response.AccountDetailsResponse;
import com.btbs.api.web.dto.accounts.response.AccountResponse;
import com.btbs.api.web.dto.common.PagedResponse;
import com.btbs.api.web.mappers.AccountApiMapper;
import com.btbs.application.accounts.*;
import com.btbs.application.accounts.dto.GetAccountBalanceQuery;
import com.btbs.application.accounts.dto.ListAccountsByCustomerQuery;
import com.btbs.domain.shared.id.AccountId;
import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import com.btbs.application.support.idempotency.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Duration;
import java.util.Map;

import java.net.URI;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping
public class AccountController {

    private final OpenAccountUseCase openAccount;
    private final DepositFundsUseCase depositFunds;
    private final WithdrawFundsUseCase withdrawFunds;
    private final TransferFundsService transferFunds;
    private final GetAccountBalanceUseCase getBalance;
    private final ListAccountsByCustomerUseCase listByCustomer;
    private final IdempotencyService idempotency;
    private final ObjectMapper objectMapper;

    public AccountController(OpenAccountUseCase openAccount,
                             DepositFundsUseCase depositFunds,
                             WithdrawFundsUseCase withdrawFunds,
                             TransferFundsService transferFunds,
                             GetAccountBalanceUseCase getBalance,
                             ListAccountsByCustomerUseCase listByCustomer,
                             IdempotencyService idempotency,
                             ObjectMapper objectMapper) {
        this.openAccount = openAccount;
        this.depositFunds = depositFunds;
        this.withdrawFunds = withdrawFunds;
        this.transferFunds = transferFunds;
        this.getBalance = getBalance;
        this.listByCustomer = listByCustomer;
        this.idempotency = idempotency;
        this.objectMapper = objectMapper;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest body) {
        var opId  = body.operationId();
        var route = "POST /accounts";
        var hash  = canonicalBodyHash(body, "operationId"); // same helper as before

        var begin = idempotency.begin(opId, route, /*actor*/ null, hash);
        if (begin.alreadyCompleted()) {
            var resp = readAccountResponse(begin.cached().responseBody());
            return ResponseEntity.ok(resp); // normalized replay = 200
        }

        AccountId id;
        try {
            id = openAccount.openAccount(AccountApiMapper.toCommand(body));
        } catch (RuntimeException ex) {
            idempotency.fail(opId);
            throw ex;
        }

        var response = new AccountResponse(id.value(), body.accountNumber());
        var json = writeJson(response);
        idempotency.commit(opId, 201, "application/json", json, java.time.Duration.ofHours(24));

        return ResponseEntity.created(java.net.URI.create("/accounts/" + id.value())).body(response);
    }

    @PostMapping("/accounts/{accountId}/deposit")
    public ResponseEntity<Void> deposit(@PathVariable UUID accountId,
                                        @Valid @RequestBody DepositRequest body) {
        depositFunds.depositFunds(AccountApiMapper.toCommand(accountId, body));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/accounts/{accountId}/withdraw")
    public ResponseEntity<Void> withdraw(@PathVariable UUID accountId,
                                         @Valid @RequestBody WithdrawRequest body) {
        withdrawFunds.withdrawFunds(AccountApiMapper.toCommand(accountId, body));
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/transfers")
    public ResponseEntity<Void> transfer(@Valid @RequestBody TransferRequest body) {
        transferFunds.transferFunds(AccountApiMapper.toCommand(body));
        return ResponseEntity.accepted().build();
    }

    @GetMapping("/accounts/{accountId}/balance")
    public ResponseEntity<Map<String, Object>> getBalance(@PathVariable UUID accountId) {
        var money = getBalance.getAccountBalance(new GetAccountBalanceQuery(accountId));
        return ResponseEntity.ok(Map.of(
                "accountId", accountId,
                "currency", money.currency().getCurrencyCode(),
                "balance", money.amount()
        ));
    }

    @GetMapping("/customers/{customerId}/accounts")
    public ResponseEntity<PagedResponse<AccountDetailsResponse>> listCustomerAccounts(
            @PathVariable UUID customerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        var result = listByCustomer.list(new ListAccountsByCustomerQuery(customerId, page, size));
        return ResponseEntity.ok(AccountApiMapper.toResponse(result));
    }

    //Helper methods for idempotency key handling - may be moved to a separate class later
    private String canonicalBodyHash(Object dto, String... ignoreFields) {
        try {
            var node = objectMapper.valueToTree(dto);
            if (node.isObject()) {
                var obj = ((com.fasterxml.jackson.databind.node.ObjectNode) node).deepCopy();
                for (String f : ignoreFields) obj.remove(f);
                return sha256Hex(objectMapper.writeValueAsString(obj));
            }
            return sha256Hex(objectMapper.writeValueAsString(node));
        } catch (Exception e) {
            return Integer.toHexString(dto.hashCode());
        }
    }

    private String writeJson(Object o) {
        try { return objectMapper.writeValueAsString(o); }
        catch (Exception e) { return "{}"; }
    }

    private AccountResponse readAccountResponse(String json) {
        try { return objectMapper.readValue(json, AccountResponse.class); }
        catch (Exception e) { throw new IllegalStateException("Invalid cached response", e); }
    }

    private static String sha256Hex(String s) {
        try {
            var md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] bytes = md.digest(s.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder(bytes.length * 2);
            for (byte b : bytes) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return s;
        }
    }

}
