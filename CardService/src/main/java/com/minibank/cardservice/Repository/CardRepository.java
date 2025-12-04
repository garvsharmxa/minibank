package com.minibank.cardservice.Repository;

import com.minibank.cardservice.Entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {

    List<Card> findByCustomerId(UUID customerId);

    List<Card> findByCardStatus(Card.CardStatus status);

}