package com.bank.account.exception;

/**
 * Exception thrown when an account is not found in the database.
 */
public class AccountNotFoundException extends RuntimeException {

    /**
     * Constructs a new AccountNotFoundException with the specified message.
     *
     * @param message the detail message
     */
    public AccountNotFoundException(String message) {
        super(message);
    }
}