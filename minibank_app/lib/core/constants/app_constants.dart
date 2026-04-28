class AppConstants {
  AppConstants._();

  static const String appName = 'MiniBank';
  static const String appTagline = 'Your Digital Banking Partner';
  static const String currency = 'USD';
  static const String currencySymbol = '\$';

  // Storage Keys
  static const String accessTokenKey = 'access_token';
  static const String refreshTokenKey = 'refresh_token';
  static const String userIdKey = 'user_id';
  static const String usernameKey = 'username';
  static const String customerIdKey = 'customer_id';
  static const String isLoggedInKey = 'is_logged_in';

  // Timeouts
  static const int connectionTimeout = 30000;
  static const int receiveTimeout = 30000;
}
