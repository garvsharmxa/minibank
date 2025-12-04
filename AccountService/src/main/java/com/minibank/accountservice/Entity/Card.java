package com.minibank.accountservice.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private UUID id;

    @Column(unique = true, nullable = false)
    private String cardNumber;

    private String cardHolderName;

    private LocalDate expiryDate;

    @Enumerated(EnumType.STRING)
    private CardType cardType;       // VISA / MASTERCARD / RUPAY

    @Enumerated(EnumType.STRING)
    private CardCategory cardCategory;  // CREDIT / DEBIT

    @Column(length = 3)
    private String cvv;

    @Column(nullable = false)
    private String pin;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    private CardStatus cardStatus;

    @CreationTimestamp
    private Timestamp createdAt;

    @UpdateTimestamp
    private Timestamp updatedAt;

    public enum CardType {
        VISA,
        MASTERCARD,
        RUPAY
    }

    public enum CardCategory {
        CREDIT,
        DEBIT
    }

    public enum CardStatus {
        ACTIVE,
        BLOCKED,
        EXPIRED
    }
}
