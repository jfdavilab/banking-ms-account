package com.bank.account.service;

import com.bank.account.client.CustomerClient;
import com.bank.account.dto.AccountRequestDto;
import com.bank.account.dto.AccountResponseDto;
import com.bank.account.exception.AccountNotFoundException;
import com.bank.account.exception.BusinessValidationException;
import com.bank.account.model.Account;
import com.bank.account.model.enums.AccountType;
import com.bank.account.repository.AccountRepository;
import com.bank.account.service.impl.AccountServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for AccountServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private CustomerClient customerClient;

    @InjectMocks
    private AccountServiceImpl accountService;

    private AccountRequestDto savingsRequest;
    private AccountRequestDto checkingRequest;
    private AccountRequestDto fixedTermRequest;
    private Account savedAccount;

    @BeforeEach
    void setUp() {
        savingsRequest = AccountRequestDto.builder()
                .accountType(AccountType.SAVINGS)
                .customerId("customer123")
                .openingAmount(new BigDecimal("500.00"))
                .transactionLimit(5)
                .build();

        checkingRequest = AccountRequestDto.builder()
                .accountType(AccountType.CHECKING)
                .customerId("customer123")
                .openingAmount(new BigDecimal("1000.00"))
                .maintenanceFee(new BigDecimal("10.00"))
                .build();

        fixedTermRequest = AccountRequestDto.builder()
                .accountType(AccountType.FIXED_TERM)
                .customerId("customer123")
                .openingAmount(new BigDecimal("2000.00"))
                .transactionDayOfMonth(15)
                .build();

        savedAccount = Account.builder()
                .id("acc001")
                .accountNumber("ABC123456789")
                .accountType(AccountType.SAVINGS)
                .customerId("customer123")
                .balance(new BigDecimal("500.00"))
                .transactionLimit(5)
                .monthlyTransactionCount(0)
                .status(true)
                .build();
    }

    @Test
    @DisplayName("Should create savings account for personal customer successfully")
    void createAccount_SavingsPersonal_Success() {
        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "PERSONAL"));
        when(accountRepository.countByCustomerIdAndAccountType(
                anyString(), eq(AccountType.SAVINGS))).thenReturn(0L);
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        AccountResponseDto result = accountService.createAccount(savingsRequest);

        assertNotNull(result);
        assertEquals(AccountType.SAVINGS, result.getAccountType());
        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    @DisplayName("Should throw exception when personal customer already has savings account")
    void createAccount_PersonalDuplicateSavings_ThrowsException() {
        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "PERSONAL"));
        when(accountRepository.countByCustomerIdAndAccountType(
                anyString(), eq(AccountType.SAVINGS))).thenReturn(1L);

        assertThrows(BusinessValidationException.class,
                () -> accountService.createAccount(savingsRequest));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when business customer tries to create savings account")
    void createAccount_BusinessSavings_ThrowsException() {
        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "BUSINESS"));

        assertThrows(BusinessValidationException.class,
                () -> accountService.createAccount(savingsRequest));
        verify(accountRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should throw exception when fixed-term account has no transaction day")
    void createAccount_FixedTermWithoutDay_ThrowsException() {
        fixedTermRequest.setTransactionDayOfMonth(null);

        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "PERSONAL"));

        assertThrows(BusinessValidationException.class,
                () -> accountService.createAccount(fixedTermRequest));
    }

    @Test
    @DisplayName("Should create checking account for business customer successfully")
    void createAccount_CheckingBusiness_Success() {
        Account checkingAccount = Account.builder()
                .id("acc002")
                .accountNumber("XYZ987654321")
                .accountType(AccountType.CHECKING)
                .customerId("customer123")
                .balance(new BigDecimal("1000.00"))
                .status(true)
                .build();

        when(customerClient.findCustomerById("customer123"))
                .thenReturn(Map.of("customerType", "BUSINESS"));
        when(accountRepository.existsByAccountNumber(anyString())).thenReturn(false);
        when(accountRepository.save(any(Account.class))).thenReturn(checkingAccount);

        AccountResponseDto result = accountService.createAccount(checkingRequest);

        assertNotNull(result);
        assertEquals(AccountType.CHECKING, result.getAccountType());
    }

    @Test
    @DisplayName("Should find account by id successfully")
    void findAccountById_Success() {
        when(accountRepository.findById("acc001")).thenReturn(Optional.of(savedAccount));

        AccountResponseDto result = accountService.findAccountById("acc001");

        assertNotNull(result);
        assertEquals("acc001", result.getId());
    }

    @Test
    @DisplayName("Should throw exception when account not found by id")
    void findAccountById_NotFound_ThrowsException() {
        when(accountRepository.findById(anyString())).thenReturn(Optional.empty());

        assertThrows(AccountNotFoundException.class,
                () -> accountService.findAccountById("nonexistent"));
    }

    @Test
    @DisplayName("Should return all accounts for a customer")
    void findAccountsByCustomerId_Success() {
        when(accountRepository.findByCustomerId("customer123"))
                .thenReturn(List.of(savedAccount));

        List<AccountResponseDto> result = accountService.findAccountsByCustomerId("customer123");

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    @Test
    @DisplayName("Should return correct balance for an account")
    void getAccountBalance_Success() {
        when(accountRepository.findById("acc001")).thenReturn(Optional.of(savedAccount));

        BigDecimal balance = accountService.getAccountBalance("acc001");

        assertEquals(new BigDecimal("500.00"), balance);
    }

    @Test
    @DisplayName("Should deactivate account successfully")
    void deleteAccount_Success() {
        when(accountRepository.findById("acc001")).thenReturn(Optional.of(savedAccount));
        when(accountRepository.save(any(Account.class))).thenReturn(savedAccount);

        accountService.deleteAccount("acc001");

        verify(accountRepository, times(1)).save(any(Account.class));
        assertFalse(savedAccount.getStatus());
    }
}