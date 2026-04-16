package com.bank.account.dto;

import com.bank.account.model.enums.AccountType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for outgoing account data in API responses.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountResponseDto {

    /** Unique account identifier. */
    private String id;

    /** Unique account number. */
    private String accountNumber;

    /** Type of bank account. */
    private AccountType accountType;

    /** ID of the primary customer owner. */
    private String customerId;

    /** Current account balance. */
    private BigDecimal balance;

    /** Minimum opening amount configured for this account. */
    private BigDecimal minimumOpeningAmount;

    /** Monthly maintenance fee. */
    private BigDecimal maintenanceFee;

    /** Maximum free transactions per month. */
    private Integer transactionLimit;

    /** Number of transactions performed this month. */
    private Integer monthlyTransactionCount;

    /** Specific day of month for fixed-term transactions. */
    private Integer transactionDayOfMonth;

    /** List of holder customer IDs. */
    private List<String> holderIds;

    /** List of authorized signer customer IDs. */
    private List<String> authorizedSignerIds;

    /** Indicates if the account is active. */
    private Boolean status;

    /** Timestamp when the account was created. */
    private LocalDateTime createdAt;

    /** Timestamp when the account was last updated. */
    private LocalDateTime updatedAt;
}