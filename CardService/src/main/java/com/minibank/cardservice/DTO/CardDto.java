package com.minibank.cardservice.DTO;

import com.minibank.cardservice.Entity.Card;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CardDto {

    private UUID id;
    private UUID customerId;
    private String cardNumber;
    private String cardHolderName;
    private LocalDate expiryDate;
    private Card.CardType cardType;
    private UUID accountId;
    private Card.CardCategory cardCategory;
    private String cvv;
    private String pin;               // <-- ADDED
    private Card.CardStatus cardStatus;
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public enum CardStatus {
        ACTIVE,
        BLOCKED,
        EXPIRED
    }
}
