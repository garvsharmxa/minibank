package com.minibank.accountservice.Controller;

import com.minibank.accountservice.DTO.TransactionDto;
import com.minibank.accountservice.Entity.Transaction;
import com.minibank.accountservice.Services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    // CREATE A NEW TRANSACTION (DEPOSIT / WITHDRAWAL)
    @PostMapping("/create/{accountId}")
    public TransactionDto createTransaction(
            @PathVariable UUID accountId,
            @RequestBody Transaction transaction
    ) {
        return transactionService.createTransaction(accountId, transaction);
    }

    // GET TRANSACTION BY ID
    @GetMapping("/{id}")
    public TransactionDto getTransactionById(@PathVariable UUID id) {
        return transactionService.getTransactionById(id);
    }

    // GET ALL TRANSACTIONS
    @GetMapping("/all")
    public List<TransactionDto> getAllTransactions() {
        return transactionService.getAllTransactions();
    }

    // GET TRANSACTIONS BY CUSTOMER ID
    @GetMapping("/customer/{customerId}")
    public List<TransactionDto> getTransactionsByCustomerId(@PathVariable UUID customerId) {
        return transactionService.getTransactionsByCustomerId(customerId);
    }

    // GET TRANSACTIONS BY ACCOUNT ID
    @GetMapping("/account/{accountId}")
    public List<TransactionDto> getTransactionsByAccountId(@PathVariable UUID accountId) {
        return transactionService.getTransactionsByAccountId(accountId);
    }

    // GET TRANSACTIONS BY STATUS (SUCCESS / FAILED / PENDING)
    @GetMapping("/status/{status}")
    public List<TransactionDto> getTransactionsByStatus(@PathVariable Transaction.TransactionStatus status) {
        return transactionService.getTransactionsByStatus(status);
    }
}
