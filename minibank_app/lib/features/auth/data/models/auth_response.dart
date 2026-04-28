class AuthResponseModel {
  final String accessToken;
  final String refreshToken;
  final String username;
  final String? userId;
  final String tokenType;
  final int? expiresIn;

  AuthResponseModel({
    required this.accessToken,
    required this.refreshToken,
    required this.username,
    this.userId,
    this.tokenType = 'Bearer',
    this.expiresIn,
  });

  factory AuthResponseModel.fromJson(Map<String, dynamic> json) {
    return AuthResponseModel(
      accessToken: json['accessToken'] ?? '',
      refreshToken: json['refreshToken'] ?? '',
      username: json['username'] ?? '',
      userId: json['userId']?.toString(),
      tokenType: json['tokenType'] ?? 'Bearer',
      expiresIn: json['expiresIn'],
    );
  }
}
