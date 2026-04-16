package com.bank.account.service;

import com.bank.account.dto.AccountRequestDto;
import com.bank.account.dto.AccountResponseDto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Service interface defining all business operations for bank accounts.
 */
public interface AccountService {

    /**
     * Creates a new bank account after validating all business rules.
     *
     * @param requestDto the account data to create
     * @return the created account as a response DTO
     */
    AccountResponseDto createAccount(AccountRequestDto requestDto);

    /**
     * Retrieves all accounts in the system.
     *
     * @return list of all accounts
     */
    List<AccountResponseDto> findAllAccounts();

    /**
     * Finds an account by its unique ID.
     *
     * @param id the account MongoDB ID
     * @return the account as a response DTO
     */
    AccountResponseDto findAccountById(String id);

    /**
     * Finds all accounts belonging to a specific customer.
     *
     * @param customerId the customer ID
     * @return list of accounts for that customer
     */
    List<AccountResponseDto> findAccountsByCustomerId(String customerId);

    /**
     * Finds an account by its account number.
     *
     * @param accountNumber the unique account number
     * @return the account as a response DTO
     */
    AccountResponseDto findAccountByAccountNumber(String accountNumber);

    /**
     * Updates an existing account's configuration.
     *
     * @param id         the account ID to update
     * @param requestDto the new account data
     * @return the updated account as a response DTO
     */
    AccountResponseDto updateAccount(String id, AccountRequestDto requestDto);

    /**
     * Performs a logical delete by setting account status to inactive.
     *
     * @param id the account ID to deactivate
     */
    void deleteAccount(String id);

    /**
     * Returns the current balance of an account.
     *
     * @param id the account ID
     * @return the current balance
     */
    BigDecimal getAccountBalance(String id);



    /**
     * Applies a deposit to an account, increasing its balance.
     *
     * @param id     the account ID
     * @param amount the deposit amount
     * @return the updated account as response DTO
     */
    AccountResponseDto applyDeposit(String id, BigDecimal amount);

    /**
     * Applies a withdrawal to an account, decreasing its balance.
     * Validates sufficient balance before executing.
     *
     * @param id     the account ID
     * @param amount the withdrawal amount
     * @return the updated account as response DTO
     */
    AccountResponseDto applyWithdrawal(String id, BigDecimal amount);
}