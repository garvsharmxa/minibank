class AccountModel {
  final String id;
  final String customerId;
  final String accountNumber;
  final String accountType;
  final String accountStatus;
  final double accountBalance;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  AccountModel({
    required this.id,
    required this.customerId,
    required this.accountNumber,
    required this.accountType,
    required this.accountStatus,
    required this.accountBalance,
    this.createdAt,
    this.updatedAt,
  });

  factory AccountModel.fromJson(Map<String, dynamic> json) {
    return AccountModel(
      id: json['id']?.toString() ?? '',
      customerId: json['customerId']?.toString() ?? '',
      accountNumber: json['accountNumber']?.toString() ?? '',
      accountType: json['accountType']?.toString() ?? 'SAVINGS',
      accountStatus: json['accountStatus']?.toString() ?? 'ACTIVE',
      accountBalance: (json['accountBalance'] ?? 0).toDouble(),
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'].toString())
          : null,
    );
  }

  bool get isActive => accountStatus == 'ACTIVE';
  bool get isBlocked => accountStatus == 'BLOCKED';
}
