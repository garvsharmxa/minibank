package com.minibank.accountservice.Services;

import com.minibank.accountservice.Config.AesEncryptor;
import com.minibank.accountservice.DTO.AccountDto;
import com.minibank.accountservice.Entity.Account;
import com.minibank.accountservice.Exception.AccountNotFoundException;
import com.minibank.accountservice.Exception.CustomerNotFoundException;
import com.minibank.accountservice.Exception.KycNotVerifiedException;
import com.minibank.accountservice.Feign.CustomerInterface;
import com.minibank.accountservice.Feign.KycInterface;
import com.minibank.accountservice.Mapper.AccountMapper;
import com.minibank.accountservice.Repository.AccountRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerInterface customerInterface;
    private final KycInterface kycInterface;   // <-- NEW (KYC microservice)

    @Autowired
    Environment environment;


    // ----------------------------------------------------------------------
    // CREATE NEW ACCOUNT
    // ----------------------------------------------------------------------
    @Transactional
    public AccountDto createAccount(Account account) {

        if (account.getCustomerId() == null) {
            throw new IllegalArgumentException("CustomerId cannot be null");
        }

        UUID customerId = account.getCustomerId();
        log.info("Validating Customer ID: {}", customerId);

        // 1️⃣ Validate Customer exists
        if (!customerInterface.customerExists(customerId)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }

        // 2️⃣ Validate KYC via KYCSERVICE
        Boolean verified = kycInterface.isKycVerified(customerId);

        if (verified == null || !verified) {
            throw new KycNotVerifiedException(
                    "Customer KYC is not verified. Cannot create account for customer: " + customerId
            );
        }

        // 3️⃣ Generate & Encrypt Account Number
        String rawAccountNumber = generateAccountNumber();
        String encrypted = AesEncryptor.encrypt(rawAccountNumber);

        account.setAccountNumber(encrypted);

        if (account.getAccountStatus() == null) {
            account.setAccountStatus(Account.AccountStatus.ACTIVE);
        }

        // Default balance
        if (account.getAccountBalance() == 0.0) {
            account.setAccountBalance(0.0);
        }

        log.info("Creating account for customer: {} with account number {}", customerId, rawAccountNumber);

        Account saved = accountRepository.save(account);

        // DECRYPT for response
        AccountDto dto = AccountMapper.toDto(saved);
        dto.setAccountNumber(rawAccountNumber);

        return dto;
    }


    // ----------------------------------------------------------------------
    // GET ACCOUNT BY ID
    // ----------------------------------------------------------------------
    public AccountDto getAccountById(UUID id) {
        Account account = validateAccount(id);
        return mapWithDecryption(account);
    }

    public boolean accountExists(UUID id) {
        return accountRepository.existsById(id);
    }

    // ----------------------------------------------------------------------
    // GET ACCOUNT BY CUSTOMER ID
    // ----------------------------------------------------------------------
    public AccountDto getAccountByCustomerId(UUID customerId) {

        if (!customerInterface.customerExists(customerId)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }

        Account account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() ->
                        new AccountNotFoundException("No account found for customerId: " + customerId));

        return mapWithDecryption(account);
    }


    // ----------------------------------------------------------------------
    // GET ALL ACCOUNTS
    // ----------------------------------------------------------------------
    public List<AccountDto> getAllAccounts() {
        System.out.println(environment.getProperty("local.server.port"));

        return accountRepository.findAll()
                .stream()
                .map(this::mapWithDecryption)
                .toList();
    }


    // ----------------------------------------------------------------------
    // UPDATE ACCOUNT
    // ----------------------------------------------------------------------
    @Transactional
    public AccountDto updateAccount(UUID id, Account updatedData) {
        Account account = validateAccount(id);

        if (updatedData.getAccountType() != null)
            account.setAccountType(updatedData.getAccountType());

        if (updatedData.getAccountStatus() != null)
            account.setAccountStatus(updatedData.getAccountStatus());

        Account updated = accountRepository.save(account);
        return mapWithDecryption(updated);
    }


    // ----------------------------------------------------------------------
    // DEPOSIT
    // ----------------------------------------------------------------------
    @Transactional
    public AccountDto deposit(UUID accountId, double amount) {

        Account account = validateAccount(accountId);
        checkIfAccountBlocked(account);

        if (amount <= 0)
            throw new IllegalArgumentException("Deposit amount must be greater than zero");

        log.info("Depositing {} to account {}", amount, accountId);

        account.setAccountBalance(account.getAccountBalance() + amount);
        Account saved = accountRepository.save(account);

        return mapWithDecryption(saved);
    }


    // ----------------------------------------------------------------------
    // WITHDRAW
    // ----------------------------------------------------------------------
    @Transactional
    public AccountDto withdraw(UUID accountId, double amount) {

        Account account = validateAccount(accountId);
        checkIfAccountBlocked(account);

        if (amount <= 0)
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");

        if (account.getAccountBalance() < amount)
            throw new IllegalArgumentException("Insufficient balance.");

        log.info("Withdrawing {} from account {}", amount, accountId);

        account.setAccountBalance(account.getAccountBalance() - amount);
        Account saved = accountRepository.save(account);

        return mapWithDecryption(saved);
    }


    // ----------------------------------------------------------------------
    // BLOCK ACCOUNT
    // ----------------------------------------------------------------------
    @Transactional
    public AccountDto blockAccount(UUID accountId) {

        Account account = validateAccount(accountId);

        if (account.getAccountStatus() == Account.AccountStatus.BLOCKED)
            throw new IllegalStateException("Account is already blocked");

        log.warn("Blocking account {}", accountId);

        account.setAccountStatus(Account.AccountStatus.BLOCKED);

        return mapWithDecryption(accountRepository.save(account));
    }


    // ----------------------------------------------------------------------
    // ACTIVATE ACCOUNT
    // ----------------------------------------------------------------------
    @Transactional
    public AccountDto activateAccount(UUID accountId) {

        Account account = validateAccount(accountId);

        if (account.getAccountStatus() == Account.AccountStatus.ACTIVE)
            throw new IllegalStateException("Account already active");

        log.info("Activating account {}", accountId);

        account.setAccountStatus(Account.AccountStatus.ACTIVE);

        return mapWithDecryption(accountRepository.save(account));
    }


    // ----------------------------------------------------------------------
    // DELETE ACCOUNT
    // ----------------------------------------------------------------------
    @Transactional
    public void deleteAccount(UUID id) {

        if (!accountRepository.existsById(id))
            throw new AccountNotFoundException("Account not found with id: " + id);

        Account account = validateAccount(id);

        if (account.getAccountBalance() > 0)
            throw new IllegalStateException("Cannot delete account with non-zero balance.");

        log.warn("Deleting account {}", id);
        accountRepository.deleteById(id);
    }


    // ----------------------------------------------------------------------
    // INTERNAL HELPERS
    // ----------------------------------------------------------------------
    private Account validateAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() ->
                        new AccountNotFoundException("Account not found with id: " + id));
    }


    private void checkIfAccountBlocked(Account account) {
        if (account.getAccountStatus() == Account.AccountStatus.BLOCKED)
            throw new IllegalStateException("Account is blocked.");

        if (account.getAccountStatus() == Account.AccountStatus.INACTIVE)
            throw new IllegalStateException("Account is inactive.");
    }


    private String generateAccountNumber() {
        String acc;
        do {
            acc = "AC" + UUID.randomUUID().toString()
                    .substring(0, 12)
                    .replace("-", "")
                    .toUpperCase();
        } while (accountRepository.findByAccountNumber(AesEncryptor.encrypt(acc)).isPresent());

        return acc;
    }


    private AccountDto mapWithDecryption(Account account) {
        AccountDto dto = AccountMapper.toDto(account);

        try {
            dto.setAccountNumber(AesEncryptor.decrypt(account.getAccountNumber()));
        } catch (Exception e) {
            dto.setAccountNumber("ERROR_DECRYPTING");
        }

        return dto;
    }
}
