package com.bank.account.mapper;

import com.bank.account.dto.AccountRequestDto;
import com.bank.account.dto.AccountResponseDto;
import com.bank.account.model.Account;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Utility class for mapping between Account entity and DTOs.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AccountMapper {

    /**
     * Converts an AccountRequestDto to an Account entity.
     * The account number is generated externally before calling this method.
     *
     * @param dto           the incoming request DTO
     * @param accountNumber the generated unique account number
     * @return a new Account entity
     */
    public static Account toEntity(AccountRequestDto dto, String accountNumber) {
        return Account.builder()
                .accountNumber(accountNumber)
                .accountType(dto.getAccountType())
                .customerId(dto.getCustomerId())
                .balance(dto.getOpeningAmount() != null
                        ? dto.getOpeningAmount()
                        : BigDecimal.ZERO)
                .minimumOpeningAmount(dto.getOpeningAmount() != null
                        ? dto.getOpeningAmount()
                        : BigDecimal.ZERO)
                .maintenanceFee(dto.getMaintenanceFee() != null
                        ? dto.getMaintenanceFee()
                        : BigDecimal.ZERO)
                .transactionLimit(dto.getTransactionLimit() != null
                        ? dto.getTransactionLimit()
                        : 0)
                .monthlyTransactionCount(0)
                .transactionDayOfMonth(dto.getTransactionDayOfMonth())
                .holderIds(dto.getHolderIds())
                .authorizedSignerIds(dto.getAuthorizedSignerIds())
                .status(true)
                .build();
    }

    /**
     * Converts an Account entity to an AccountResponseDto.
     *
     * @param account the account entity
     * @return the response DTO
     */
    public static AccountResponseDto toDto(Account account) {
        return AccountResponseDto.builder()
                .id(account.getId())
                .accountNumber(account.getAccountNumber())
                .accountType(account.getAccountType())
                .customerId(account.getCustomerId())
                .balance(account.getBalance())
                .minimumOpeningAmount(account.getMinimumOpeningAmount())
                .maintenanceFee(account.getMaintenanceFee())
                .transactionLimit(account.getTransactionLimit())
                .monthlyTransactionCount(account.getMonthlyTransactionCount())
                .transactionDayOfMonth(account.getTransactionDayOfMonth())
                .holderIds(account.getHolderIds())
                .authorizedSignerIds(account.getAuthorizedSignerIds())
                .status(account.getStatus())
                .createdAt(account.getCreatedAt())
                .updatedAt(account.getUpdatedAt())
                .build();
    }

    /**
     * Updates an existing Account entity with data from a request DTO.
     *
     * @param existing the existing account entity
     * @param dto      the incoming request DTO
     * @return the updated Account entity
     */
    public static Account updateEntity(Account existing, AccountRequestDto dto) {
        existing.setMaintenanceFee(dto.getMaintenanceFee() != null
                ? dto.getMaintenanceFee()
                : BigDecimal.ZERO);
        existing.setTransactionLimit(dto.getTransactionLimit() != null
                ? dto.getTransactionLimit()
                : 0);
        existing.setTransactionDayOfMonth(dto.getTransactionDayOfMonth());
        existing.setHolderIds(dto.getHolderIds());
        existing.setAuthorizedSignerIds(dto.getAuthorizedSignerIds());
        return existing;
    }
}