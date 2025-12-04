package com.minibank.accountservice.Repository;

import com.minibank.accountservice.Entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByCustomerId(UUID customerId);

    List<Card> findByCardStatus(Card.CardStatus status);

}