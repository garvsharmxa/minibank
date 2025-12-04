package com.minibank.accountservice.Mapper;

import com.minibank.accountservice.DTO.CardDto;
import com.minibank.accountservice.Entity.Card;

public class CardMapper {

    // Convert Entity → DTO
    public static CardDto toDto(Card card) {
        if (card == null) return null;

        CardDto dto = new CardDto();
        if (card.getAccount() != null) {
            dto.setAccountId(card.getAccount().getId());
        }
        dto.setId(card.getId());
        dto.setCustomerId(card.getCustomerId());
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

        // DO NOT set account here; service will fetch it
        return card;
    }
}
