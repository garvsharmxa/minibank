package com.minibank.cardservice.Mapper;

import com.minibank.cardservice.DTO.CardDto;
import com.minibank.cardservice.Entity.Card;

public class CardMapper {

    // Convert Entity → DTO
    public static CardDto toDto(Card card) {
        if (card == null) return null;

        CardDto dto = new CardDto();

        dto.setId(card.getId());
        dto.setCustomerId(card.getCustomerId());
        dto.setAccountId(card.getAccountId());
        dto.setCardNumber(card.getCardNumber());
        dto.setCardHolderName(card.getCardHolderName());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardType(card.getCardType());
        dto.setCardCategory(card.getCardCategory());
        dto.setCvv(card.getCvv());
        dto.setPin(card.getPin());
        dto.setCardStatus(card.getCardStatus());
        dto.setCreatedAt(card.getCreatedAt());
        dto.setUpdatedAt(card.getUpdatedAt());

        return dto;
    }

    // Convert DTO → Entity
    public static Card toEntity(CardDto dto) {
        if (dto == null) return null;

        Card card = new Card();

        card.setId(dto.getId());
        card.setCustomerId(dto.getCustomerId());
        card.setAccountId(dto.getAccountId());
        card.setCardNumber(dto.getCardNumber());
        card.setCardHolderName(dto.getCardHolderName());
        card.setExpiryDate(dto.getExpiryDate());
        card.setCardType(dto.getCardType());
        card.setCardCategory(dto.getCardCategory());
        card.setCvv(dto.getCvv());
        card.setPin(dto.getPin());
        card.setCardStatus(dto.getCardStatus());
        card.setCreatedAt(dto.getCreatedAt());
        card.setUpdatedAt(dto.getUpdatedAt());

        return card;
    }
}
