package com.minibank.accountservice.DTO;

import com.minibank.accountservice.Entity.Transaction;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.sql.Timestamp;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionDto {

    private UUID id;
    private UUID customerId;
    private UUID accountId;
    private Transaction.TransactionType transactionType;
    private Transaction.TransactionMethod transactionMethod;
    private Transaction.TransactionStatus transactionStatus;
    private Double amount;
    private String referenceId;
    private Double openingBalance;
    private Double closingBalance;
    private Timestamp transactionDate;
}
