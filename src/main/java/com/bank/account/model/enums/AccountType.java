package com.bank.account.model.enums;

/**
 * Defines the types of bank accounts available in the system.
 */
public enum AccountType {
    /**
     * Savings account: no maintenance fee,
     * limited monthly transactions.
     */
    SAVINGS,

    /**
     * Checking account: has maintenance fee,
     * unlimited monthly transactions.
     */
    CHECKING,

    /**
     * Fixed-term account: no maintenance fee,
     * only one transaction allowed on a specific day of the month.
     */
    FIXED_TERM
}