class KycStatusModel {
  final bool exists;
  final bool verified;
  final String? kycId;

  KycStatusModel({
    required this.exists,
    required this.verified,
    this.kycId,
  });

  factory KycStatusModel.fromJson(Map<String, dynamic> json) {
    return KycStatusModel(
      exists: json['exists'] ?? false,
      verified: json['verified'] ?? false,
      kycId: json['kycId']?.toString(),
    );
  }
}
