package com.minibank.accountservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Enumerated(EnumType.STRING)
    private TransactionType transactionType;

    @Enumerated(EnumType.STRING)
    private TransactionMethod transactionMethod;

    @Enumerated(EnumType.STRING)
    private TransactionStatus transactionStatus;

    private Double amount;

    private String referenceId;

    private Double openingBalance;

    private Double closingBalance;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @CreationTimestamp
    private Timestamp transactionDate;

    public enum TransactionType {
        DEPOSIT,
        WITHDRAWAL,
        TRANSFER
    }

    public enum TransactionMethod {
        UPI,
        NET_BANKING,
        CARD,
        CHEQUE,
        ATM
    }

    public enum TransactionStatus {
        FAILED,
        SUCCESS,
        PENDING
    }
}
