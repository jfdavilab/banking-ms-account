package com.bank.account.controller;

import com.bank.account.dto.AccountRequestDto;
import com.bank.account.dto.AccountResponseDto;
import com.bank.account.service.AccountService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

/**
 * REST controller for bank account management operations.
 * Exposes endpoints following REST conventions for CRUD operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    /**
     * Creates a new bank account.
     * POST /api/v1/accounts
     *
     * @param requestDto the account data
     * @return HTTP 201 with the created account
     */
    @PostMapping
    public ResponseEntity<AccountResponseDto> createAccount(
            @Valid @RequestBody AccountRequestDto requestDto) {
        log.info("POST /api/v1/accounts - Creating account type: {}", requestDto.getAccountType());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(accountService.createAccount(requestDto));
    }

    /**
     * Retrieves all accounts.
     * GET /api/v1/accounts
     *
     * @return HTTP 200 with list of all accounts
     */
    @GetMapping
    public ResponseEntity<List<AccountResponseDto>> getAllAccounts() {
        log.info("GET /api/v1/accounts - Retrieving all accounts");
        return ResponseEntity.ok(accountService.findAllAccounts());
    }

    /**
     * Retrieves an account by its ID.
     * GET /api/v1/accounts/{id}
     *
     * @param id the account ID
     * @return HTTP 200 with the account data
     */
    @GetMapping("/{id}")
    public ResponseEntity<AccountResponseDto> getAccountById(@PathVariable String id) {
        log.info("GET /api/v1/accounts/{} - Retrieving account by id", id);
        return ResponseEntity.ok(accountService.findAccountById(id));
    }

    /**
     * Retrieves an account by its account number.
     * GET /api/v1/accounts/number/{accountNumber}
     *
     * @param accountNumber the unique account number
     * @return HTTP 200 with the account data
     */
    @GetMapping("/number/{accountNumber}")
    public ResponseEntity<AccountResponseDto> getAccountByNumber(
            @PathVariable String accountNumber) {
        log.info("GET /api/v1/accounts/number/{} - Retrieving account", accountNumber);
        return ResponseEntity.ok(accountService.findAccountByAccountNumber(accountNumber));
    }

    /**
     * Retrieves all accounts for a specific customer.
     * GET /api/v1/accounts/customer/{customerId}
     *
     * @param customerId the customer ID
     * @return HTTP 200 with list of customer accounts
     */
    @GetMapping("/customer/{customerId}")
    public ResponseEntity<List<AccountResponseDto>> getAccountsByCustomerId(
            @PathVariable String customerId) {
        log.info("GET /api/v1/accounts/customer/{} - Retrieving accounts", customerId);
        return ResponseEntity.ok(accountService.findAccountsByCustomerId(customerId));
    }

    /**
     * Retrieves the current balance of an account.
     * GET /api/v1/accounts/{id}/balance
     *
     * @param id the account ID
     * @return HTTP 200 with the current balance
     */
    @GetMapping("/{id}/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(@PathVariable String id) {
        log.info("GET /api/v1/accounts/{}/balance - Getting balance", id);
        return ResponseEntity.ok(accountService.getAccountBalance(id));
    }

    /**
     * Updates an existing account.
     * PUT /api/v1/accounts/{id}
     *
     * @param id         the account ID to update
     * @param requestDto the updated account data
     * @return HTTP 200 with the updated account
     */
    @PutMapping("/{id}")
    public ResponseEntity<AccountResponseDto> updateAccount(
            @PathVariable String id,
            @Valid @RequestBody AccountRequestDto requestDto) {
        log.info("PUT /api/v1/accounts/{} - Updating account", id);
        return ResponseEntity.ok(accountService.updateAccount(id, requestDto));
    }

    /**
     * Deactivates an account (logical delete).
     * DELETE /api/v1/accounts/{id}
     *
     * @param id the account ID to deactivate
     * @return HTTP 204 No Content
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAccount(@PathVariable String id) {
        log.info("DELETE /api/v1/accounts/{} - Deactivating account", id);
        accountService.deleteAccount(id);
        return ResponseEntity.noContent().build();
    }


    /**
     * Applies a deposit to an account (called by ms-transaction).
     * PATCH /api/v1/accounts/{id}/deposit
     *
     * @param id     the account ID
     * @param amount the amount to deposit
     * @return HTTP 200 with updated account
     */
    @PatchMapping("/{id}/deposit")
    public ResponseEntity<AccountResponseDto> applyDeposit(
            @PathVariable String id,
            @RequestParam @DecimalMin(value = "0.01",
                    message = "Amount must be greater than zero") BigDecimal amount) {
        log.info("PATCH /api/v1/accounts/{}/deposit - amount: {}", id, amount);
        return ResponseEntity.ok(accountService.applyDeposit(id, amount));
    }

    /**
     * Applies a withdrawal to an account (called by ms-transaction).
     * PATCH /api/v1/accounts/{id}/withdrawal
     *
     * @param id     the account ID
     * @param amount the amount to withdraw
     * @return HTTP 200 with updated account
     */
    @PatchMapping("/{id}/withdrawal")
    public ResponseEntity<AccountResponseDto> applyWithdrawal(
            @PathVariable String id,
            @RequestParam @DecimalMin(value = "0.01",
                    message = "Amount must be greater than zero") BigDecimal amount) {
        log.info("PATCH /api/v1/accounts/{}/withdrawal - amount: {}", id, amount);
        return ResponseEntity.ok(accountService.applyWithdrawal(id, amount));
    }
}