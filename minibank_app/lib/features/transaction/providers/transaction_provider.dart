import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/models/transaction_model.dart';
import '../data/transaction_api_service.dart';

@immutable
class TransactionState {
  final bool isLoading;
  final List<TransactionModel> transactions;
  final String? error;
  final bool isCreating;
  final String? selectedFilter;

  const TransactionState({
    this.isLoading = false,
    this.transactions = const [],
    this.error,
    this.isCreating = false,
    this.selectedFilter,
  });

  TransactionState copyWith({
    bool? isLoading,
    List<TransactionModel>? transactions,
    String? error,
    bool? isCreating,
    String? selectedFilter,
  }) {
    return TransactionState(
      isLoading: isLoading ?? this.isLoading,
      transactions: transactions ?? this.transactions,
      error: error,
      isCreating: isCreating ?? this.isCreating,
      selectedFilter: selectedFilter ?? this.selectedFilter,
    );
  }

  List<TransactionModel> get recentTransactions {
    final sorted = [...transactions];
    sorted.sort((a, b) {
      final aDate = a.transactionDate ?? DateTime(2000);
      final bDate = b.transactionDate ?? DateTime(2000);
      return bDate.compareTo(aDate);
    });
    return sorted.take(5).toList();
  }
}

class TransactionNotifier extends StateNotifier<TransactionState> {
  final TransactionApiService _service;

  TransactionNotifier()
      : _service = TransactionApiService(),
        super(const TransactionState());

  Future<void> fetchTransactionsByAccount(String accountId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final transactions = await _service.getTransactionsByAccountId(accountId);
      state = state.copyWith(isLoading: false, transactions: transactions);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> fetchTransactionsByCustomer(String customerId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final transactions = await _service.getTransactionsByCustomerId(customerId);
      state = state.copyWith(isLoading: false, transactions: transactions);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<bool> createTransaction(
    String accountId,
    double amount,
    String type, {
    String? description,
    String? targetAccountId,
  }) async {
    state = state.copyWith(isCreating: true, error: null);
    try {
      final transaction = TransactionModel(
        id: '',
        customerId: '',
        accountId: accountId,
        transactionType: type,
        transactionMethod: 'ONLINE',
        transactionStatus: 'PENDING',
        amount: amount,
        description: description,
        targetAccountId: targetAccountId,
      );
      final created = await _service.createTransaction(accountId, transaction);
      state = state.copyWith(
        isCreating: false,
        transactions: [created, ...state.transactions],
      );
      return true;
    } catch (e) {
      state = state.copyWith(isCreating: false, error: e.toString());
      return false;
    }
  }

  void setFilter(String? filter) {
    state = state.copyWith(selectedFilter: filter);
  }
}

final transactionProvider =
    StateNotifierProvider<TransactionNotifier, TransactionState>((ref) {
  return TransactionNotifier();
});
