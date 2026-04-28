import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import '../data/account_api_service.dart';
import '../data/models/account_model.dart';

@immutable
class AccountState {
  final bool isLoading;
  final List<AccountModel> accounts;
  final AccountModel? selectedAccount;
  final String? error;
  final bool isOperating;

  const AccountState({
    this.isLoading = false,
    this.accounts = const [],
    this.selectedAccount,
    this.error,
    this.isOperating = false,
  });

  AccountState copyWith({
    bool? isLoading,
    List<AccountModel>? accounts,
    AccountModel? selectedAccount,
    String? error,
    bool? isOperating,
  }) {
    return AccountState(
      isLoading: isLoading ?? this.isLoading,
      accounts: accounts ?? this.accounts,
      selectedAccount: selectedAccount ?? this.selectedAccount,
      error: error,
      isOperating: isOperating ?? this.isOperating,
    );
  }

  double get totalBalance => accounts.fold(0.0, (sum, acc) => sum + acc.accountBalance);
}

class AccountNotifier extends StateNotifier<AccountState> {
  final AccountApiService _service;

  AccountNotifier() : _service = AccountApiService(), super(const AccountState());

  Future<void> fetchAccounts(String customerId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final accounts = await _service.getAccountsByCustomerId(customerId);
      state = state.copyWith(isLoading: false, accounts: accounts);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<void> fetchAccountById(String accountId) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final account = await _service.getAccountById(accountId);
      state = state.copyWith(isLoading: false, selectedAccount: account);
    } catch (e) {
      state = state.copyWith(isLoading: false, error: e.toString());
    }
  }

  Future<bool> deposit(String accountId, double amount) async {
    state = state.copyWith(isOperating: true, error: null);
    try {
      final updated = await _service.deposit(accountId, amount);
      final accounts = state.accounts.map((a) {
        return a.id == accountId ? updated : a;
      }).toList();
      state = state.copyWith(
        isOperating: false,
        accounts: accounts,
        selectedAccount: updated,
      );
      return true;
    } catch (e) {
      state = state.copyWith(isOperating: false, error: e.toString());
      return false;
    }
  }

  Future<bool> withdraw(String accountId, double amount) async {
    state = state.copyWith(isOperating: true, error: null);
    try {
      final updated = await _service.withdraw(accountId, amount);
      final accounts = state.accounts.map((a) {
        return a.id == accountId ? updated : a;
      }).toList();
      state = state.copyWith(
        isOperating: false,
        accounts: accounts,
        selectedAccount: updated,
      );
      return true;
    } catch (e) {
      state = state.copyWith(isOperating: false, error: e.toString());
      return false;
    }
  }

  Future<bool> createAccount(String customerId, String accountType) async {
    state = state.copyWith(isOperating: true, error: null);
    try {
      final account = await _service.createAccount(
        customerId: customerId,
        accountType: accountType,
      );
      state = state.copyWith(
        isOperating: false,
        accounts: [...state.accounts, account],
      );
      return true;
    } catch (e) {
      state = state.copyWith(isOperating: false, error: e.toString());
      return false;
    }
  }

  void selectAccount(AccountModel account) {
    state = state.copyWith(selectedAccount: account);
  }
}

final accountProvider = StateNotifierProvider<AccountNotifier, AccountState>((ref) {
  return AccountNotifier();
});
