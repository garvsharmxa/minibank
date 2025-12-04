package com.minibank.accountservice.Services;

import com.minibank.accountservice.Config.AesEncryptor;
import com.minibank.accountservice.DTO.TransactionDto;
import com.minibank.accountservice.Entity.Account;
import com.minibank.accountservice.Entity.Transaction;
import com.minibank.accountservice.Mapper.TransactionMapper;
import com.minibank.accountservice.Repository.AccountRepository;
import com.minibank.accountservice.Repository.TransactionRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    // CREATE TRANSACTION (DEPOSIT / WITHDRAWAL)
    public TransactionDto createTransaction(UUID accountId, Transaction transaction) {

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + accountId));

        transaction.setOpeningBalance(account.getAccountBalance());

        // Handle deposit
        if (transaction.getTransactionType() == Transaction.TransactionType.DEPOSIT) {
            account.setAccountBalance(account.getAccountBalance() + transaction.getAmount());
        }
        // Handle withdrawal
        else if (transaction.getTransactionType() == Transaction.TransactionType.WITHDRAWAL) {

            if (account.getAccountBalance() < transaction.getAmount()) {
                transaction.setTransactionStatus(Transaction.TransactionStatus.FAILED);
                transaction.setClosingBalance(account.getAccountBalance());
                transaction.setReferenceId(encryptReferenceId());
                return TransactionMapper.toDto(transactionRepository.save(transaction));
            }

            account.setAccountBalance(account.getAccountBalance() - transaction.getAmount());
        }

        transaction.setClosingBalance(account.getAccountBalance());
        transaction.setTransactionStatus(Transaction.TransactionStatus.SUCCESS);
        transaction.setReferenceId(encryptReferenceId());
        transaction.setAccount(account);
        transaction.setCustomerId(account.getCustomerId());

        transactionRepository.save(transaction);
        accountRepository.save(account);

        return TransactionMapper.toDto(transaction);
    }

    // GET BY ID
    public TransactionDto getTransactionById(UUID id) {
        Transaction t = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with id: " + id));
        return TransactionMapper.toDto(t);
    }

    // GET ALL
    public List<TransactionDto> getAllTransactions() {
        return transactionRepository.findAll()
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // GET BY CUSTOMER ID
    public List<TransactionDto> getTransactionsByCustomerId(UUID customerId) {
        return transactionRepository.findByCustomerId(customerId)
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // GET BY ACCOUNT ID
    public List<TransactionDto> getTransactionsByAccountId(UUID accountId) {
        return transactionRepository.findByAccountId(accountId)
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }

    // GET BY STATUS
    public List<TransactionDto> getTransactionsByStatus(Transaction.TransactionStatus status) {
        return transactionRepository.findByTransactionStatus(status)
                .stream()
                .map(TransactionMapper::toDto)
                .collect(Collectors.toList());
    }


    // ENCRYPTED REFERENCE ID GENERATOR
    private String encryptReferenceId() {
        String raw = "TXN-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return AesEncryptor.encrypt(raw);
    }
}
