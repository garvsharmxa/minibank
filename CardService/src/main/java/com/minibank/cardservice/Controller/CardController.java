package com.minibank.cardservice.Controller;

import com.minibank.cardservice.DTO.CardDto;
import com.minibank.cardservice.Entity.Card;
import com.minibank.cardservice.Service.CardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardService cardService;

    // CREATE CARD
    @PostMapping("/create")
    public CardDto createCard(@RequestBody CardDto cardDto) {
        return cardService.createCard(cardDto);
    }

    // GET CARD BY ID
    @GetMapping("/{id}")
    public CardDto getCardById(@PathVariable UUID id) {
        return cardService.getCardById(id);
    }

    // GET ALL CARDS
    @GetMapping("/all")
    public List<CardDto> getAllCards() {
        return cardService.getAllCards();
    }

    // BLOCK CARD
    @PatchMapping("/{id}/block")
    public CardDto blockCard(@PathVariable UUID id) {
        return cardService.blockCard(id);
    }

    // UPDATE CARD PIN
    @PatchMapping("/{id}/update-pin")
    public CardDto updatePin(
            @PathVariable UUID id,
            @RequestParam String newPin
    ) {
        return cardService.updateCardPin(id, newPin);
    }

    @PatchMapping("/{id}/status")
    public CardDto updateCardStatus(
            @PathVariable UUID id,
            @RequestParam Card.CardStatus status
    ) {
        return cardService.updateCardStatus(id,status);
    }

    // GET CARDS BY CUSTOMER ID
    @GetMapping("/customer/{customerId}")
    public List<CardDto> getCardsByCustomer(@PathVariable UUID customerId) {
        return cardService.getCardsByCustomerId(customerId);
    }

    // GET ACTIVE CARDS ONLY
    @GetMapping("/active")
    public List<CardDto> getActiveCards() {
        return cardService.getActiveCards();
    }

    // DELETE CARD
    @DeleteMapping("/{id}")
    public String deleteCard(@PathVariable UUID id) {
        cardService.deleteCard(id);
        return "Card deleted successfully!";
    }
}
