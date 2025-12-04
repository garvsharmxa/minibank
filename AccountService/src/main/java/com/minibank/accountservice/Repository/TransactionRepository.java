package com.minibank.accountservice.Repository;

import com.minibank.accountservice.Entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    List<Transaction> findByCustomerId(UUID customerId);

    List<Transaction> findByAccountId(UUID accountId);

    List<Transaction> findByTransactionStatus(Transaction.TransactionStatus status);

}