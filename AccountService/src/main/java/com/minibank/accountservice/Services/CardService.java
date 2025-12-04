package com.minibank.accountservice.Services;

import com.minibank.accountservice.Config.AesEncryptor;
import com.minibank.accountservice.DTO.CardDto;
import com.minibank.accountservice.Entity.Account;
import com.minibank.accountservice.Entity.Card;
import com.minibank.accountservice.Mapper.CardMapper;
import com.minibank.accountservice.Repository.AccountRepository;
import com.minibank.accountservice.Repository.CardRepository;
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
    private final AccountRepository accountRepository;
    private final Random random = new Random();

    // CREATE CARD
    public CardDto createCard(CardDto cardDto) {

        // Fetch the account first
        Account account = accountRepository.findById(cardDto.getAccountId())
                .orElseThrow(() -> new RuntimeException("Account not found with id: " + cardDto.getAccountId()));

        // Map DTO → Entity
        Card card = CardMapper.toEntity(cardDto);

        // Set the account (important!)
        card.setAccount(account);

        // Generate random card details (user cannot set these)
        card.setCardNumber(generateCardNumber());
        card.setExpiryDate(generateExpiryDate());
        card.setCvv(generateCvv());
        String generatedPin = generatePin();

        // Encrypt card number & PIN before saving
        card.setCardNumber(AesEncryptor.encrypt(card.getCardNumber()));
        card.setPin(AesEncryptor.encrypt(generatedPin));

        // Save card
        card = cardRepository.save(card);

        // Return decrypted DTO
        return decryptDto(CardMapper.toDto(card));
    }

    // GET CARD BY ID
    public CardDto getCardById(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));
        return decryptDto(CardMapper.toDto(card));
    }

    // GET ALL CARDS
    public List<CardDto> getAllCards() {
        return cardRepository.findAll()
                .stream()
                .map(CardMapper::toDto)
                .map(this::decryptDto)
                .collect(Collectors.toList());
    }

    // BLOCK CARD
    public CardDto blockCard(UUID id) {
        Card card = cardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + id));

        card.setCardStatus(Card.CardStatus.BLOCKED);
        cardRepository.save(card);

        return decryptDto(CardMapper.toDto(card));
    }

    // UPDATE CARD PIN
    public CardDto updateCardPin(UUID cardId, String newPin) {
        if (newPin == null || newPin.length() != 4) {
            throw new IllegalArgumentException("PIN must be a 4-digit number.");
        }

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        // Encrypt new PIN
        card.setPin(AesEncryptor.encrypt(newPin));
        cardRepository.save(card);

        return decryptDto(CardMapper.toDto(card));
    }

    // UPDATE CARD STATUS
    public CardDto updateCardStatus(UUID cardId, Card.CardStatus status) {
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("Card not found with id: " + cardId));

        card.setCardStatus(status);
        cardRepository.save(card);

        return decryptDto(CardMapper.toDto(card));
    }

    // GET CARDS BY CUSTOMER ID
    public List<CardDto> getCardsByCustomerId(UUID customerId) {
        return cardRepository.findByCustomerId(customerId)
                .stream()
                .map(CardMapper::toDto)
                .map(this::decryptDto)
                .collect(Collectors.toList());
    }

    // DELETE CARD
    public void deleteCard(UUID id) {
        if (!cardRepository.existsById(id)) {
            throw new RuntimeException("Card not found with id: " + id);
        }
        cardRepository.deleteById(id);
    }

    // GET ACTIVE CARDS ONLY
    public List<CardDto> getActiveCards() {
        return cardRepository.findByCardStatus(Card.CardStatus.ACTIVE)
                .stream()
                .map(CardMapper::toDto)
                .map(this::decryptDto)
                .collect(Collectors.toList());
    }

    // INTERNAL: Decrypt DTO fields
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

    // GENERATE RANDOM CARD NUMBER (16 digits, Luhn algorithm compliant)
    private String generateCardNumber() {
        StringBuilder cardNumber = new StringBuilder();

        // First 6 digits (BIN/IIN) - using a common test range
        cardNumber.append("400000");

        // Next 9 digits random
        for (int i = 0; i < 9; i++) {
            cardNumber.append(random.nextInt(10));
        }

        // Last digit (Luhn check digit)
        int checkDigit = calculateLuhnCheckDigit(cardNumber.toString());
        cardNumber.append(checkDigit);

        return cardNumber.toString();
    }

    // Calculate Luhn check digit
    private int calculateLuhnCheckDigit(String number) {
        int sum = 0;
        boolean alternate = true;

        for (int i = number.length() - 1; i >= 0; i--) {
            int digit = Character.getNumericValue(number.charAt(i));

            if (alternate) {
                digit *= 2;
                if (digit > 9) {
                    digit = (digit % 10) + 1;
                }
            }
            sum += digit;
            alternate = !alternate;
        }

        return (10 - (sum % 10)) % 10;
    }

    // GENERATE RANDOM EXPIRY DATE (3-5 years from now)
    private LocalDate generateExpiryDate() {
        LocalDate today = LocalDate.now();
        int yearsToAdd = 3 + random.nextInt(3); // 3 to 5 years
        return today.plusYears(yearsToAdd).withDayOfMonth(1);
    }

    // GENERATE RANDOM CVV (3 digits)
    private String generateCvv() {
        int cvv = 100 + random.nextInt(900); // 100-999
        return String.valueOf(cvv);
    }

    // GENERATE RANDOM PIN (4 digits)
    private String generatePin() {
        int pin = 1000 + random.nextInt(9000); // 1000-9999
        return String.valueOf(pin);
    }
}