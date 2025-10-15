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
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

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

    public AccountController(OpenAccountUseCase openAccount,
                             DepositFundsUseCase depositFunds,
                             WithdrawFundsUseCase withdrawFunds,
                             TransferFundsService transferFunds,
                             GetAccountBalanceUseCase getBalance,
                             ListAccountsByCustomerUseCase listByCustomer) {
        this.openAccount = openAccount;
        this.depositFunds = depositFunds;
        this.withdrawFunds = withdrawFunds;
        this.transferFunds = transferFunds;
        this.getBalance = getBalance;
        this.listByCustomer = listByCustomer;
    }

    @PostMapping("/accounts")
    public ResponseEntity<AccountResponse> open(@Valid @RequestBody OpenAccountRequest body) {
        AccountId id = openAccount.openAccount(AccountApiMapper.toCommand(body));
        var response = new AccountResponse(id.value(), body.accountNumber());
        return ResponseEntity
                .created(URI.create("/accounts/" + id.value()))
                .body(response);
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

}
