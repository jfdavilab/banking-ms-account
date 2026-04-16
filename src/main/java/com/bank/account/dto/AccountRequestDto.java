package com.bank.account.dto;

import com.bank.account.model.enums.AccountType;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO for incoming account creation and update requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AccountRequestDto {

    /** Type of account to create. */
    @NotNull(message = "Account type is required")
    private AccountType accountType;

    /** ID of the primary customer owner. */
    @NotBlank(message = "Customer ID is required")
    private String customerId;

    /** Initial deposit amount. Must be >= 0. */
    @NotNull(message = "Opening amount is required")
    @DecimalMin(value = "0.0", message = "Opening amount must be zero or greater")
    private BigDecimal openingAmount;

    /**
     * Monthly maintenance fee.
     * Required for CHECKING accounts.
     */
    @Builder.Default
    @DecimalMin(value = "0.0", message = "Maintenance fee must be zero or greater")
    private BigDecimal maintenanceFee = BigDecimal.ZERO;

    /**
     * Maximum number of free monthly transactions.
     * Required for SAVINGS accounts.
     */
    @Builder.Default
    @Min(value = 0, message = "Transaction limit must be zero or greater")
    private Integer transactionLimit = 0;

    /**
     * Day of the month when the single transaction is allowed.
     * Required for FIXED_TERM accounts (1-31).
     */
    @Min(value = 1, message = "Transaction day must be between 1 and 31")
    @Max(value = 31, message = "Transaction day must be between 1 and 31")
    private Integer transactionDayOfMonth;

    /**
     * List of additional holder customer IDs.
     * Applicable only for business accounts.
     */
    @Builder.Default
    private List<String> holderIds = new ArrayList<>();

    /**
     * List of authorized signer customer IDs.
     * Applicable only for business accounts.
     */
    @Builder.Default
    private List<String> authorizedSignerIds = new ArrayList<>();
}