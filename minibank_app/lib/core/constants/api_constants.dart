import 'dart:io';

class ApiConstants {
  ApiConstants._();

  // Base URL - uses 10.0.2.2 for Android emulator, localhost for iOS
  static String get baseUrl {
    if (Platform.isAndroid) {
      return 'http://10.0.2.2';
    }
    return 'http://localhost';
  }

  // Service Ports
  static const int authPort = 8081;
  static const int customerPort = 8082;
  static const int accountPort = 8083;
  static const int kycPort = 8084;
  static const int cardPort = 8085;
  static const int transactionPort = 8086;

  // Service Base URLs
  static String get authBaseUrl => '$baseUrl:$authPort';
  static String get customerBaseUrl => '$baseUrl:$customerPort';
  static String get accountBaseUrl => '$baseUrl:$accountPort';
  static String get kycBaseUrl => '$baseUrl:$kycPort';
  static String get cardBaseUrl => '$baseUrl:$cardPort';
  static String get transactionBaseUrl => '$baseUrl:$transactionPort';

  // Auth Endpoints
  static const String register = '/auth/register';
  static const String login = '/auth/login';
  static const String refreshToken = '/auth/refresh';
  static const String logout = '/auth/logout';
  static const String changePassword = '/auth/change-password';
  static const String users = '/api/users';

  // Customer Endpoints
  static const String customers = '/customers';

  // Account Endpoints
  static const String accounts = '/accounts';

  // Transaction Endpoints
  static const String transactions = '/transactions';

  // Card Endpoints
  static const String cards = '/cards';

  // KYC Endpoints
  static const String kyc = '/kyc';
}
