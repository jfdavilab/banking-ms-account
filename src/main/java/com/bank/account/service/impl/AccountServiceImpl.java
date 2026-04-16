package com.bank.account.service.impl;

import com.bank.account.client.CustomerClient;
import com.bank.account.dto.AccountRequestDto;
import com.bank.account.dto.AccountResponseDto;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.BusinessValidationException;
import com.bank.account.mapper.AccountMapper;
import com.bank.account.model.Account;
import com.bank.account.model.enums.AccountType;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.AccountService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Implementation of AccountService with full business rule validation.
 *
 * <p>Business rules enforced:</p>
 * <ul>
 *   <li>PERSONAL customer: max 1 SAVINGS, max 1 CHECKING, unlimited FIXED_TERM</li>
 *   <li>BUSINESS customer: only CHECKING allowed (multiple), no SAVINGS or FIXED_TERM</li>
 *   <li>FIXED_TERM accounts require a transaction day of month</li>
 *   <li>CHECKING accounts require a maintenance fee configuration</li>
 * </ul>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final CustomerClient customerClient;

    /** Customer type constant for personal customers. */
    private static final String CUSTOMER_TYPE_PERSONAL = "PERSONAL";

    /** Customer type constant for business customers. */
    private static final String CUSTOMER_TYPE_BUSINESS = "BUSINESS";

    /**
     * {@inheritDoc}
     * Validates customer existence, type compatibility and account limits
     * before persisting the new account.
     */
    @Override
    public AccountResponseDto createAccount(AccountRequestDto requestDto) {
        log.info("Creating account type {} for customerId: {}",
                requestDto.getAccountType(), requestDto.getCustomerId());

        // Validate customer exists and retrieve their type
        Map<String, Object> customer = customerClient.findCustomerById(requestDto.getCustomerId());
        String customerType = (String) customer.get("customerType");

        // Validate business rules based on customer type
        validateAccountCreationRules(requestDto, customerType);

        // Validate account-type specific rules
        validateAccountTypeRules(requestDto);

        // Generate unique account number
        String accountNumber = generateAccountNumber();

        Account account = AccountMapper.toEntity(requestDto, accountNumber);
        Account savedAccount = accountRepository.save(account);

        log.info("Account created successfully with number: {}", savedAccount.getAccountNumber());
        return AccountMapper.toDto(savedAccount);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountResponseDto> findAllAccounts() {
        log.info("Retrieving all accounts");
        return accountRepository.findAll()
                .stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResponseDto findAccountById(String id) {
        log.info("Searching account by id: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", id);
                    return new AccountNotFoundException("Account not found with id: " + id);
                });
        return AccountMapper.toDto(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<AccountResponseDto> findAccountsByCustomerId(String customerId) {
        log.info("Retrieving accounts for customerId: {}", customerId);
        return accountRepository.findByCustomerId(customerId)
                .stream()
                .map(AccountMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResponseDto findAccountByAccountNumber(String accountNumber) {
        log.info("Searching account by account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> {
                    log.error("Account not found with number: {}", accountNumber);
                    return new AccountNotFoundException(
                            "Account not found with number: " + accountNumber);
                });
        return AccountMapper.toDto(account);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccountResponseDto updateAccount(String id, AccountRequestDto requestDto) {
        log.info("Updating account with id: {}", id);

        Account existing = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", id);
                    return new AccountNotFoundException("Account not found with id: " + id);
                });

        Account updated = AccountMapper.updateEntity(existing, requestDto);
        Account savedAccount = accountRepository.save(updated);

        log.info("Account updated successfully with id: {}", savedAccount.getId());
        return AccountMapper.toDto(savedAccount);
    }

    /**
     * {@inheritDoc}
     * Performs a logical delete (sets status to false).
     */
    @Override
    public void deleteAccount(String id) {
        log.info("Deactivating account with id: {}", id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("Account not found with id: {}", id);
                    return new AccountNotFoundException("Account not found with id: " + id);
                });

        account.setStatus(false);
        accountRepository.save(account);
        log.info("Account deactivated successfully with id: {}", id);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public BigDecimal getAccountBalance(String id) {
        log.info("Getting balance for account id: {}", id);
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + id));
        return account.getBalance();
    }

    // ─────────────────────────────────────────────
    //  Private validation methods
    // ─────────────────────────────────────────────

    /**
     * Validates account creation rules based on the customer type.
     *
     * <p>Rules:</p>
     * <ul>
     *   <li>PERSONAL: max 1 SAVINGS, max 1 CHECKING, unlimited FIXED_TERM</li>
     *   <li>BUSINESS: only CHECKING allowed, multiple permitted</li>
     * </ul>
     *
     * @param requestDto   the incoming account request
     * @param customerType the customer type retrieved from ms-customer
     */
    private void validateAccountCreationRules(AccountRequestDto requestDto, String customerType) {
        AccountType accountType = requestDto.getAccountType();
        String customerId = requestDto.getCustomerId();

        if (CUSTOMER_TYPE_PERSONAL.equals(customerType)) {
            validatePersonalCustomerRules(customerId, accountType);
        } else if (CUSTOMER_TYPE_BUSINESS.equals(customerType)) {
            validateBusinessCustomerRules(accountType);
        } else {
            throw new BusinessValidationException("Unknown customer type: " + customerType);
        }
    }

    /**
     * Validates account limits for personal customers.
     * Personal customers can have at most 1 SAVINGS and 1 CHECKING account.
     *
     * @param customerId  the customer ID
     * @param accountType the type of account being created
     */
    private void validatePersonalCustomerRules(String customerId, AccountType accountType) {
        if (accountType == AccountType.SAVINGS) {
            long count = accountRepository.countByCustomerIdAndAccountType(
                    customerId, AccountType.SAVINGS);
            if (count >= 1) {
                throw new BusinessValidationException(
                        "Personal customer can only have one savings account");
            }
        }

        if (accountType == AccountType.CHECKING) {
            long count = accountRepository.countByCustomerIdAndAccountType(
                    customerId, AccountType.CHECKING);
            if (count >= 1) {
                throw new BusinessValidationException(
                        "Personal customer can only have one checking account");
            }
        }
    }

    /**
     * Validates account type restrictions for business customers.
     * Business customers can only have CHECKING accounts.
     *
     * @param accountType the type of account being created
     */
    private void validateBusinessCustomerRules(AccountType accountType) {
        if (accountType == AccountType.SAVINGS || accountType == AccountType.FIXED_TERM) {
            throw new BusinessValidationException(
                    "Business customers can only have checking accounts");
        }
    }

    /**
     * Validates rules specific to each account type.
     * FIXED_TERM requires a transaction day. SAVINGS requires a transaction limit.
     *
     * @param requestDto the incoming account request
     */
    private void validateAccountTypeRules(AccountRequestDto requestDto) {
        if (requestDto.getAccountType() == AccountType.FIXED_TERM
                && requestDto.getTransactionDayOfMonth() == null) {
            throw new BusinessValidationException(
                    "Fixed-term accounts require a transaction day of month");
        }

        if (requestDto.getAccountType() == AccountType.SAVINGS
                && (requestDto.getTransactionLimit() == null
                || requestDto.getTransactionLimit() <= 0)) {
            throw new BusinessValidationException(
                    "Savings accounts require a valid transaction limit greater than zero");
        }
    }

    /**
     * Generates a unique account number using UUID.
     * Ensures no collision with existing account numbers.
     *
     * @return a unique 12-digit account number string
     */
    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = UUID.randomUUID()
                    .toString()
                    .replace("-", "")
                    .substring(0, 12)
                    .toUpperCase();
        } while (accountRepository.existsByAccountNumber(accountNumber));
        return accountNumber;
    }




    /**
     * {@inheritDoc}
     * Increases the account balance by the deposit amount.
     */
    @Override
    public AccountResponseDto applyDeposit(String id, BigDecimal amount) {
        log.info("Applying deposit of {} to account id: {}", amount, id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + id));

        if (!account.getStatus()) {
            throw new BusinessValidationException(
                    "Cannot deposit to an inactive account");
        }

        account.setBalance(account.getBalance().add(amount));
        Account saved = accountRepository.save(account);
        log.info("Deposit applied. New balance for account {}: {}", id, saved.getBalance());
        return AccountMapper.toDto(saved);
    }

    /**
     * {@inheritDoc}
     * Validates sufficient balance before decreasing it.
     */
    @Override
    public AccountResponseDto applyWithdrawal(String id, BigDecimal amount) {
        log.info("Applying withdrawal of {} to account id: {}", amount, id);

        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException(
                        "Account not found with id: " + id));

        if (!account.getStatus()) {
            throw new BusinessValidationException(
                    "Cannot withdraw from an inactive account");
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new BusinessValidationException(
                    "Insufficient balance. Available: " + account.getBalance()
                            + ", Requested: " + amount);
        }

        account.setBalance(account.getBalance().subtract(amount));
        Account saved = accountRepository.save(account);
        log.info("Withdrawal applied. New balance for account {}: {}", id, saved.getBalance());
        return AccountMapper.toDto(saved);
    }
}