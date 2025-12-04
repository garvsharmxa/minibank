package com.minibank.cardservice.Service;

import com.minibank.cardservice.ClientDto.AccountDTO;
import com.minibank.cardservice.Config.AesEncryptor;
import com.minibank.cardservice.DTO.CardDto;
import com.minibank.cardservice.Entity.Card;
import com.minibank.cardservice.Feign.AccountInterface;
import com.minibank.cardservice.Mapper.CardMapper;
import com.minibank.cardservice.Repository.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CardService {

    private final CardRepository cardRepository;
    private final AccountInterface accountInterface;  // ✔ FEIGN client
    private final Random random = new Random();

    // ---------------- CREATE CARD ----------------
    public CardDto createCard(CardDto cardDto) {

        // 1. Validate account using Feign
        Boolean exists = accountInterface.accountExists(cardDto.getAccountId());
        if (exists == null || !exists) {
            throw new RuntimeException("Account does not exist: " + cardDto.getAccountId());
        }

        // Fetch account details
        AccountDTO account = accountInterface.getAccountById(cardDto.getAccountId());

        // 2. Convert DTO → Entity
        Card card = CardMapper.toEntity(cardDto);

        // Set foreign keys
        card.setAccountId(account.getId());
        card.setCustomerId(account.getCustomerId());

        // 3. Generate secure card details
        card.setCardNumber(AesEncryptor.encrypt(generateCardNumber()));
        card.setExpiryDate(generateExpiryDate());
        card.setCvv(generateCvv());
        card.setPin(AesEncryptor.encrypt(generatePin()));
        card.setCardStatus(Card.CardStatus.ACTIVE);

        // 4. Save card
        Card saved = cardRepository.save(card);

        // 5. Return decrypted DTO
        return decryptDto(CardMapper.toDto(saved));
    }

    // ---------------- GET ONE ----------------
    public CardDto getCardById(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        return decryptDto(CardMapper.toDto(card));
    }

    // ---------------- GET ALL ----------------
    public List<CardDto> getAllCards() {
        return cardRepository.findAll().stream()
                .map(CardMapper::toDto)
                .map(this::decryptDto)
                .collect(Collectors.toList());
    }

    // ---------------- BLOCK CARD ----------------
    public CardDto blockCard(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setCardStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);

        return decryptDto(CardMapper.toDto(card));
    }

    // ---------------- UPDATE PIN ----------------
    public CardDto updateCardPin(UUID cardId, String newPin) {
        if (newPin == null || newPin.length() != 4) {
            throw new IllegalArgumentException("PIN must be 4 digits");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setPin(AesEncryptor.encrypt(newPin));
        cardRepository.save(card);

        return decryptDto(CardMapper.toDto(card));
    }

    // ---------------- UPDATE STATUS ----------------
    public CardDto updateCardStatus(UUID cardId, Card.CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found"));

        card.setCardStatus(status);
        cardRepository.save(card);

        return decryptDto(CardMapper.toDto(card));
    }

    // ---------------- GET BY CUSTOMER ID ----------------
    public List<CardDto> getCardsByCustomerId(UUID customerId) {
        return cardRepository.findByCustomerId(customerId)
                .stream()
                .map(CardMapper::toDto)
                .map(this::decryptDto)
                .collect(Collectors.toList());
    }

    // ---------------- DELETE ----------------
    public void deleteCard(UUID id) {
        if (!cardRepository.existsById(id)) {
            throw new RuntimeException("Card not found");
        }
        cardRepository.deleteById(id);
    }

    // ---------------- ACTIVE CARDS ----------------
    public List<CardDto> getActiveCards() {
        return cardRepository.findByCardStatus(Card.CardStatus.ACTIVE)
                .stream()
                .map(CardMapper::toDto)
                .map(this::decryptDto)
                .collect(Collectors.toList());
    }

    // ---------------- INTERNAL: DECRYPT ----------------
    private CardDto decryptDto(CardDto dto) {

        try {
            dto.setCardNumber(AesEncryptor.decrypt(dto.getCardNumber()));
        } catch (Exception e) {
            dto.setCardNumber("ERROR_DECRYPTING");
        }

        try {
            dto.setPin(AesEncryptor.decrypt(dto.getPin()));
        } catch (Exception e) {
            dto.setPin("ERROR_DECRYPTING");
        }

        return dto;
    }

    // ---------------- GENERATORS ----------------
    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder("400000"); // Visa BIN

        for (int i = 0; i < 9; i++) {
            cardNumber.append(random.nextInt(10));
        }

        cardNumber.append(calculateLuhnCheckDigit(cardNumber.toString()));
        return cardNumber.toString();
    }

    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int n = number.charAt(i) - '0';
            if (alternate) {
                n *= 2;
                if (n > 9) n = n % 10 + 1;
            }
            sum += n;
            alternate = !alternate;
        }
        return (10 - (sum % 10)) % 10;
    }

    private LocalDate generateExpiryDate() {
        return LocalDate.now().plusYears(4).withDayOfMonth(1);
    }

    private String generateCvv() {
        return String.valueOf(100 + random.nextInt(900));
    }

    private String generatePin() {
        return String.valueOf(1000 + random.nextInt(9000));
    }
}
