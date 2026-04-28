class CardModel {
  final String id;
  final String customerId;
  final String cardNumber;
  final String cardHolderName;
  final String? expiryDate;
  final String cardType;
  final String accountId;
  final String? cardCategory;
  final String? cvv;
  final String? pin;
  final String cardStatus;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  CardModel({
    required this.id,
    required this.customerId,
    required this.cardNumber,
    required this.cardHolderName,
    this.expiryDate,
    required this.cardType,
    required this.accountId,
    this.cardCategory,
    this.cvv,
    this.pin,
    required this.cardStatus,
    this.createdAt,
    this.updatedAt,
  });

  factory CardModel.fromJson(Map<String, dynamic> json) {
    return CardModel(
      id: json['id']?.toString() ?? '',
      customerId: json['customerId']?.toString() ?? '',
      cardNumber: json['cardNumber']?.toString() ?? '',
      cardHolderName: json['cardHolderName']?.toString() ?? '',
      expiryDate: json['expiryDate']?.toString(),
      cardType: json['cardType']?.toString() ?? 'DEBIT',
      accountId: json['accountId']?.toString() ?? '',
      cardCategory: json['cardCategory']?.toString(),
      cvv: json['cvv']?.toString(),
      pin: json['pin']?.toString(),
      cardStatus: json['cardStatus']?.toString() ?? 'ACTIVE',
      createdAt: json['createdAt'] != null
          ? DateTime.tryParse(json['createdAt'].toString())
          : null,
      updatedAt: json['updatedAt'] != null
          ? DateTime.tryParse(json['updatedAt'].toString())
          : null,
    );
  }

  bool get isActive => cardStatus == 'ACTIVE';
  bool get isBlocked => cardStatus == 'BLOCKED';
  bool get isExpired => cardStatus == 'EXPIRED';
  bool get isDebit => cardType == 'DEBIT';
  bool get isCredit => cardType == 'CREDIT';
}
