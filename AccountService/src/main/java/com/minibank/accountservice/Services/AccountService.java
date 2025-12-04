package com.minibank.accountservice. Services;

import com.minibank.accountservice.Client.CustomerClient;
import com.minibank. accountservice.Client.CustomerDTO;
import com. minibank.accountservice.Config.AesEncryptor;
import com.minibank.accountservice.DTO.AccountDto;
import com.minibank.accountservice.Entity.Account;
import com.minibank.accountservice.Exception.AccountNotFoundException;
import com. minibank.accountservice.Exception.CustomerNotFoundException;
import com.minibank.accountservice.Exception.KycNotVerifiedException;
import com.minibank.accountservice.Mapper.AccountMapper;
import com.minibank.accountservice.Repository.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j. Slf4j;
import org. springframework.stereotype.Service;
import org.springframework.transaction.annotation. Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private final AccountRepository accountRepository;
    private final CustomerClient customerClient;

    // CREATE NEW ACCOUNT
    @Transactional
    public AccountDto createAccount(Account account) {
        if (account.getCustomerId() == null) {
            throw new IllegalArgumentException("CustomerId cannot be null");
        }

        // Validate customer exists in CustomerService
        log.info("Validating customer with ID: {}", account.getCustomerId());
        if (!customerClient.customerExists(account.getCustomerId())) {
            throw new CustomerNotFoundException("Customer not found with ID: " + account.getCustomerId());
        }

        // Fetch customer details to validate KYC
        CustomerDTO customer = customerClient.getCustomerDetails(account.getCustomerId());

        // Check if KYC is verified before creating account
        if (customer.getKyc() == null || !customer.getKyc().isVerified()) {
            throw new KycNotVerifiedException("Customer KYC is not verified.  Cannot create account for customer: " + account.getCustomerId());
        }

        // Generate & Encrypt Account Number
        String rawAccountNumber = generateAccountNumber();
        String encryptedAccountNumber = AesEncryptor.encrypt(rawAccountNumber);

        account.setAccountNumber(encryptedAccountNumber);

        if (account.getAccountStatus() == null) {
            account.setAccountStatus(Account.AccountStatus.ACTIVE);
        }

        // Initialize balance to 0 if not set
        if (account.getAccountBalance() == 0.0) {
            account.setAccountBalance(0.0);
        }

        log.info("Creating account for customer: {} with account number: {}", account.getCustomerId(), rawAccountNumber);
        Account savedAccount = accountRepository.save(account);

        // Return DTO with DECRYPTED account number
        AccountDto dto = AccountMapper.toDto(savedAccount);
        dto. setAccountNumber(rawAccountNumber);

        return dto;
    }

    // GET ACCOUNT BY ID
    public AccountDto getAccountById(UUID id) {
        Account account = validateAccount(id);
        return mapWithDecryption(account);
    }

    // GET ACCOUNT BY CUSTOMER ID
    public AccountDto getAccountByCustomerId(UUID customerId) {
        // Validate customer exists before fetching account
        if (!customerClient.customerExists(customerId)) {
            throw new CustomerNotFoundException("Customer not found with ID: " + customerId);
        }

        Account account = accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new AccountNotFoundException("No account found for customerId: " + customerId));
        return mapWithDecryption(account);
    }

    // GET ALL ACCOUNTS
    public List<AccountDto> getAllAccounts() {
        return accountRepository. findAll().stream()
                . map(this::mapWithDecryption)
                .toList();
    }

    // UPDATE ACCOUNT
    @Transactional
    public AccountDto updateAccount(UUID id, Account updatedData) {
        Account account = validateAccount(id);

        if (updatedData.getAccountType() != null) {
            account.setAccountType(updatedData.getAccountType());
        }
        if (updatedData.getAccountStatus() != null) {
            account.setAccountStatus(updatedData.getAccountStatus());
        }

        Account updated = accountRepository.save(account);

        return mapWithDecryption(updated);
    }

    // DEPOSIT
    @Transactional
    public AccountDto deposit(UUID accountId, double amount) {
        Account account = validateAccount(accountId);
        checkIfAccountBlocked(account);

        if (amount <= 0) {
            throw new IllegalArgumentException("Deposit amount must be greater than zero");
        }

        log.info("Depositing {} to account: {}", amount, accountId);
        account.setAccountBalance(account.getAccountBalance() + amount);
        Account saved = accountRepository.save(account);

        return mapWithDecryption(saved);
    }

    // WITHDRAW
    @Transactional
    public AccountDto withdraw(UUID accountId, double amount) {
        Account account = validateAccount(accountId);
        checkIfAccountBlocked(account);

        if (amount <= 0) {
            throw new IllegalArgumentException("Withdrawal amount must be greater than zero");
        }

        if (account. getAccountBalance() < amount) {
            throw new IllegalArgumentException("Insufficient balance.  Available: " + account.getAccountBalance() + ", Requested: " + amount);
        }

        log. info("Withdrawing {} from account: {}", amount, accountId);
        account.setAccountBalance(account.getAccountBalance() - amount);
        Account saved = accountRepository.save(account);

        return mapWithDecryption(saved);
    }

    // BLOCK ACCOUNT
    @Transactional
    public AccountDto blockAccount(UUID accountId) {
        Account account = validateAccount(accountId);

        if (account.getAccountStatus() == Account.AccountStatus.BLOCKED) {
            throw new IllegalStateException("Account is already blocked");
        }

        log.warn("Blocking account: {}", accountId);
        account.setAccountStatus(Account.AccountStatus. BLOCKED);
        Account saved = accountRepository.save(account);

        return mapWithDecryption(saved);
    }

    // ACTIVATE ACCOUNT
    @Transactional
    public AccountDto activateAccount(UUID accountId) {
        Account account = validateAccount(accountId);

        if (account.getAccountStatus() == Account.AccountStatus. ACTIVE) {
            throw new IllegalStateException("Account is already active");
        }

        log.info("Activating account: {}", accountId);
        account.setAccountStatus(Account.AccountStatus. ACTIVE);
        Account saved = accountRepository.save(account);

        return mapWithDecryption(saved);
    }

    // DELETE ACCOUNT
    @Transactional
    public void deleteAccount(UUID id) {
        if (! accountRepository.existsById(id)) {
            throw new AccountNotFoundException("Account not found with id: " + id);
        }

        Account account = validateAccount(id);

        // Check if account has balance before deletion
        if (account. getAccountBalance() > 0) {
            throw new IllegalStateException("Cannot delete account with non-zero balance.  Current balance: " + account.getAccountBalance());
        }

        log.warn("Deleting account: {}", id);
        accountRepository.deleteById(id);
    }

    // CHECK IF CUSTOMER HAS ACCOUNT
    public boolean customerHasAccount(UUID customerId) {
        return accountRepository.findByCustomerId(customerId).isPresent();
    }

    // INTERNAL VALIDATOR
    private Account validateAccount(UUID id) {
        return accountRepository.findById(id)
                .orElseThrow(() -> new AccountNotFoundException("Account not found with id: " + id));
    }

    // INTERNAL: CHECK BLOCKED ACCOUNT
    private void checkIfAccountBlocked(Account account) {
        if (account.getAccountStatus() == Account.AccountStatus.BLOCKED) {
            throw new IllegalStateException("Account is blocked. No transactions allowed.");
        }
        if (account.getAccountStatus() == Account.AccountStatus.INACTIVE) {
            throw new IllegalStateException("Account is inactive. Please activate the account first.");
        }
    }

    // SECURE ACCOUNT NUMBER GEN
    private String generateAccountNumber() {
        String accountNumber;
        do {
            accountNumber = "AC" + UUID.randomUUID().toString().substring(0, 12).replace("-", ""). toUpperCase();
        } while (accountRepository.findByAccountNumber(AesEncryptor.encrypt(accountNumber)).isPresent());

        return accountNumber;
    }

    // MAP (ENTITY → DTO) with DECRYPTION
    private AccountDto mapWithDecryption(Account account) {
        AccountDto dto = AccountMapper. toDto(account);

        try {
            String decrypted = AesEncryptor.decrypt(account.getAccountNumber());
            dto.setAccountNumber(decrypted);
        } catch (Exception e) {
            log.error("Error decrypting account number for account: {}", account.getId(), e);
            dto.setAccountNumber("ERROR_DECRYPTING");
        }

        return dto;
    }
}