package com.minibank.transactionservice.Service;

import com.minibank.transactionservice.ClientDto.AccountDTO;
import com.minibank.transactionservice.Config.AesEncryptor;
import com.minibank.transactionservice.DTO.TransactionDto;
import com.minibank.transactionservice.Entity.Transaction;
import com.minibank.transactionservice.Fegin.AccountInterface;
import com.minibank.transactionservice.Mapper.TransactionMapper;
import com.minibank.transactionservice.Repository.TransactionRepository;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountInterface accountInterface;  // <-- FEIGN CLIENT

    // ----------------------------------------------------------------
    // CREATE TRANSACTION (Deposit / Withdrawal)
    // ----------------------------------------------------------------
    public TransactionDto createTransaction(UUID accountId, Transaction transaction) {

        // 1️⃣ FETCH ACCOUNT FROM ACCOUNT-SERVICE
        AccountDTO account = accountInterface.getAccountById(accountId);
        if (account == null) {
            throw new RuntimeException("Account not found with id: " + accountId);
        }

        double openingBalance = account.getAccountBalance();
        transaction.setOpeningBalance(openingBalance);

        // 2️⃣ PERFORM TRANSACTION LOGIC
        double amount = transaction.getAmount();

        if (transaction.getTransactionType() == Transaction.TransactionType.DEPOSIT) {

            // Update account balance via AccountService
            accountInterface.deposit(accountId, amount);

        } else if (transaction.getTransactionType() == Transaction.TransactionType.WITHDRAWAL) {

            if (openingBalance < amount) {
                // Failed transaction
                transaction.setTransactionStatus(Transaction.TransactionStatus.FAILED);
                transaction.setClosingBalance(openingBalance);
                transaction.setReferenceId(encryptReferenceId());
                return TransactionMapper.toDto(transactionRepository.save(transaction));
            }

            // Call AccountService to withdraw
            accountInterface.withdraw(accountId, amount);
        }

        // 3️⃣ FETCH UPDATED ACCOUNT BALANCE FROM ACCOUNT-SERVICE
        AccountDTO updatedAccount = accountInterface.getAccountById(accountId);

        transaction.setClosingBalance(updatedAccount.getAccountBalance());
        transaction.setTransactionStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setReferenceId(encryptReferenceId());
        transaction.setAccountId(accountId);
        transaction.setCustomerId(updatedAccount.getCustomerId());

        // 4️⃣ SAVE TRANSACTION IN DB
        Transaction saved = transactionRepository.save(transaction);

        return TransactionMapper.toDto(saved);
    }

    // ----------------------------------------------------------------
    // GET TRANSACTION BY ID
    // ----------------------------------------------------------------
    public TransactionDto getTransactionById(UUID id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return TransactionMapper.toDto(t);
    }

    // ----------------------------------------------------------------
    // GET ALL TRANSACTIONS
    // ----------------------------------------------------------------
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // GET TRANSACTIONS BY CUSTOMER ID
    // ----------------------------------------------------------------
    public List<TransactionDto> getTransactionsByCustomerId(UUID customerId) {
        return transactionRepository.findByCustomerId(customerId)
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // GET TRANSACTIONS BY ACCOUNT ID
    // ----------------------------------------------------------------
    public List<TransactionDto> getTransactionsByAccountId(UUID accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // GET TRANSACTIONS BY STATUS
    // ----------------------------------------------------------------
    public List<TransactionDto> getTransactionsByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByTransactionStatus(status)
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // GENERATE ENCRYPTED REFERENCE ID
    // ----------------------------------------------------------------
    private String encryptReferenceId() {
        String raw = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return AesEncryptor.encrypt(raw);
    }
}
