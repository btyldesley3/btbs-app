package com.btbs.api.web.controllers;

import com.btbs.api.web.dto.accounts.request.DepositRequest;
import com.btbs.api.web.dto.accounts.request.OpenAccountRequest;
import com.btbs.api.web.dto.accounts.request.TransferRequest;
import com.btbs.api.web.dto.accounts.request.WithdrawRequest;
import com.btbs.api.web.dto.accounts.response.AccountResponse;
import com.btbs.api.web.mappers.AccountApiMapper;
import com.btbs.application.accounts.DepositFundsUseCase;
import com.btbs.application.accounts.OpenAccountUseCase;
import com.btbs.application.accounts.TransferFundsService;
import com.btbs.application.accounts.WithdrawFundsUseCase;
import com.btbs.domain.shared.id.AccountId;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping
public class AccountController {

    private final OpenAccountUseCase openAccount;
    private final DepositFundsUseCase depositFunds;
    private final WithdrawFundsUseCase withdrawFunds;
    private final TransferFundsService transferFunds;

    public AccountController(OpenAccountUseCase openAccount,
                             DepositFundsUseCase depositFunds,
                             WithdrawFundsUseCase withdrawFunds,
                             TransferFundsService transferFunds) {
        this.openAccount = openAccount;
        this.depositFunds = depositFunds;
        this.withdrawFunds = withdrawFunds;
        this.transferFunds = transferFunds;
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

}
