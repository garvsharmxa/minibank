import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/card_api_service.dart';
import '../data/models/card_model.dart';

@immutable
class CardState {
  final bool isLoading;
  final List<CardModel> cards;
  final String? error;
  final bool isOperating;

  const CardState({
    this.isLoading = false,
    this.cards = const [],
    this.error,
    this.isOperating = false,
  });

  CardState copyWith({
    bool? isLoading,
    List<CardModel>? cards,
    String? error,
    bool? isOperating,
  }) {
    return CardState(
      isLoading: isLoading ?? this.isLoading,
      cards: cards ?? this.cards,
      error: error,
      isOperating: isOperating ?? this.isOperating,
    );
  }
}

class CardNotifier extends StateNotifier<CardState> {
  final CardApiService _service;

  CardNotifier() : _service = CardApiService(), super(const CardState());

  Future<void> fetchCards(String customerId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final cards = await _service.getCardsByCustomerId(customerId);
      state = state.copyWith(isLoading: false, cards: cards);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<bool> createCard({
    required String accountId,
    required String customerId,
    required String cardType,
    required String pin,
  }) async {
    state = state.copyWith(isOperating: true, error: null);
    try {
      final card = await _service.createCard(
        accountId: accountId,
        customerId: customerId,
        cardType: cardType,
        pin: pin,
      );
      state = state.copyWith(
        isOperating: false,
        cards: [...state.cards, card],
      );
      return true;
    } catch (e) {
      state = state.copyWith(isOperating: false, error: e.toString());
      return false;
    }
  }

  Future<bool> blockCard(String cardId) async {
    state = state.copyWith(isOperating: true, error: null);
    try {
      final updated = await _service.blockCard(cardId);
      final cards = state.cards.map((c) => c.id == cardId ? updated : c).toList();
      state = state.copyWith(isOperating: false, cards: cards);
      return true;
    } catch (e) {
      state = state.copyWith(isOperating: false, error: e.toString());
      return false;
    }
  }

  Future<bool> updatePin(String cardId, String newPin) async {
    state = state.copyWith(isOperating: true, error: null);
    try {
      final updated = await _service.updatePin(cardId, newPin);
      final cards = state.cards.map((c) => c.id == cardId ? updated : c).toList();
      state = state.copyWith(isOperating: false, cards: cards);
      return true;
    } catch (e) {
      state = state.copyWith(isOperating: false, error: e.toString());
      return false;
    }
  }

  Future<bool> activateCard(String cardId) async {
    state = state.copyWith(isOperating: true, error: null);
    try {
      final updated = await _service.updateCardStatus(cardId, 'ACTIVE');
      final cards = state.cards.map((c) => c.id == cardId ? updated : c).toList();
      state = state.copyWith(isOperating: false, cards: cards);
      return true;
    } catch (e) {
      state = state.copyWith(isOperating: false, error: e.toString());
      return false;
    }
  }
}

final cardProvider = StateNotifierProvider<CardNotifier, CardState>((ref) {
  return CardNotifier();
});
