import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter_secure_storage/flutter_secure_storage.dart';
import '../../../core/constants/app_constants.dart';
import '../data/auth_api_service.dart';
import '../data/models/auth_response.dart';
import '../data/models/login_request.dart';
import '../data/models/register_request.dart';

// Auth State
@immutable
class AuthState {
  final bool isLoading;
  final bool isAuthenticated;
  final String? username;
  final String? userId;
  final String? email;
  final String? error;

  const AuthState({
    this.isLoading = false,
    this.isAuthenticated = false,
    this.username,
    this.userId,
    this.email,
    this.error,
  });

  AuthState copyWith({
    bool? isLoading,
    bool? isAuthenticated,
    String? username,
    String? userId,
    String? email,
    String? error,
  }) {
    return AuthState(
      isLoading: isLoading ?? this.isLoading,
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      username: username ?? this.username,
      userId: userId ?? this.userId,
      email: email ?? this.email,
      error: error,
    );
  }
}

// Auth Notifier
class AuthNotifier extends StateNotifier<AuthState> {
  final AuthApiService _authService;
  final FlutterSecureStorage _storage;

  AuthNotifier()
      : _authService = AuthApiService(),
        _storage = const FlutterSecureStorage(),
        super(const AuthState());

  Future<void> checkAuthStatus() async {
    final token = await _storage.read(key: AppConstants.accessTokenKey);
    final username = await _storage.read(key: AppConstants.usernameKey);
    final userId = await _storage.read(key: AppConstants.userIdKey);
    final email = await _storage.read(key: 'user_email');

    if (token != null) {
      state = AuthState(
        isAuthenticated: true,
        username: username,
        userId: userId,
        email: email,
      );
    } else {
      state = const AuthState(isAuthenticated: false);
    }
  }

  Future<bool> login(String usernameOrEmail, String password) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      final authResponse = await _authService.login(
        LoginRequest(email: usernameOrEmail, password: password),
      );
      await _saveTokens(authResponse);
      state = AuthState(
        isAuthenticated: true,
        username: authResponse.username,
        userId: authResponse.userId,
        isLoading: false,
      );
      return true;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
      return false;
    }
  }

  /// Register and auto-login. Returns true if both succeed.
  Future<bool> registerAndLogin(
      String username, String email, String password) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      // 1. Register
      await _authService.register(
        RegisterRequest(username: username, email: email, password: password),
      );

      // 2. Auto-login
      final authResponse = await _authService.login(
        LoginRequest(email: username, password: password),
      );
      await _saveTokens(authResponse);
      // Save email for customer profile creation
      await _storage.write(key: 'user_email', value: email);
      state = AuthState(
        isAuthenticated: true,
        username: authResponse.username,
        userId: authResponse.userId,
        email: email,
        isLoading: false,
      );
      return true;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
      return false;
    }
  }

  Future<bool> register(String username, String email, String password) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      await _authService.register(
        RegisterRequest(username: username, email: email, password: password),
      );
      state = state.copyWith(isLoading: false);
      return true;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
      return false;
    }
  }

  Future<void> logout() async {
    try {
      final refreshToken =
          await _storage.read(key: AppConstants.refreshTokenKey);
      await _authService.logout(refreshToken);
    } catch (_) {
      // Proceed with local logout even if API call fails
    }
    await _clearTokens();
    state = const AuthState(isAuthenticated: false);
  }

  Future<bool> changePassword(
      String currentPassword, String newPassword) async {
    state = state.copyWith(isLoading: true, error: null);
    try {
      await _authService.changePassword(currentPassword, newPassword);
      state = state.copyWith(isLoading: false);
      return true;
    } catch (e) {
      state = state.copyWith(
        isLoading: false,
        error: _parseError(e),
      );
      return false;
    }
  }

  /// Get user's email from storage
  Future<String?> getStoredEmail() async {
    return await _storage.read(key: 'user_email');
  }

  Future<void> _saveTokens(AuthResponseModel response) async {
    await _storage.write(
        key: AppConstants.accessTokenKey, value: response.accessToken);
    await _storage.write(
        key: AppConstants.refreshTokenKey, value: response.refreshToken);
    await _storage.write(
        key: AppConstants.usernameKey, value: response.username);
    if (response.userId != null) {
      await _storage.write(
          key: AppConstants.userIdKey, value: response.userId);
    }
    await _storage.write(key: AppConstants.isLoggedInKey, value: 'true');
  }

  Future<void> _clearTokens() async {
    await _storage.deleteAll();
  }

  String _parseError(dynamic error) {
    if (error.toString().contains('DioException')) {
      if (error.toString().contains('connection')) {
        return 'Unable to connect to server. Please check your connection.';
      }
      return 'Server error. Please try again.';
    }
    return error.toString().replaceAll('Exception: ', '');
  }
}

// Providers
final authProvider = StateNotifierProvider<AuthNotifier, AuthState>((ref) {
  return AuthNotifier();
});
