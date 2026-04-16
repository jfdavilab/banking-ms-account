package com.bank.account.repository;

import com.bank.account.model.Account;
import com.bank.account.model.enums.AccountType;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Account entity.
 * Uses Spring Data derived queries — no @Query annotations.
 */
@Repository
public interface AccountRepository extends MongoRepository<Account, String> {

    /**
     * Finds all accounts belonging to a specific customer.
     *
     * @param customerId the customer ID
     * @return list of accounts for that customer
     */
    List<Account> findByCustomerId(String customerId);

    /**
     * Finds all accounts of a specific type for a customer.
     *
     * @param customerId  the customer ID
     * @param accountType the type of account
     * @return list of matching accounts
     */
    List<Account> findByCustomerIdAndAccountType(String customerId, AccountType accountType);

    /**
     * Counts how many accounts of a given type a customer has.
     *
     * @param customerId  the customer ID
     * @param accountType the account type to count
     * @return the number of accounts
     */
    long countByCustomerIdAndAccountType(String customerId, AccountType accountType);

    /**
     * Finds an account by its unique account number.
     *
     * @param accountNumber the account number
     * @return an Optional with the account if found
     */
    Optional<Account> findByAccountNumber(String accountNumber);

    /**
     * Finds all active or inactive accounts for a customer.
     *
     * @param customerId the customer ID
     * @param status     true for active, false for inactive
     * @return list of accounts matching the criteria
     */
    List<Account> findByCustomerIdAndStatus(String customerId, Boolean status);

    /**
     * Checks if an account number already exists.
     *
     * @param accountNumber the account number to check
     * @return true if it exists
     */
    boolean existsByAccountNumber(String accountNumber);
}