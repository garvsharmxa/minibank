class TransactionModel {
  final String id;
  final String customerId;
  final String accountId;
  final String transactionType;
  final String transactionMethod;
  final String transactionStatus;
  final double amount;
  final String? referenceId;
  final double? openingBalance;
  final double? closingBalance;
  final DateTime? transactionDate;
  final String? description;
  final String? targetAccountId;

  TransactionModel({
    required this.id,
    required this.customerId,
    required this.accountId,
    required this.transactionType,
    required this.transactionMethod,
    required this.transactionStatus,
    required this.amount,
    this.referenceId,
    this.openingBalance,
    this.closingBalance,
    this.transactionDate,
    this.description,
    this.targetAccountId,
  });

  factory TransactionModel.fromJson(Map<String, dynamic> json) {
    return TransactionModel(
      id: json['id']?.toString() ?? '',
      customerId: json['customerId']?.toString() ?? '',
      accountId: json['accountId']?.toString() ?? '',
      transactionType: json['transactionType']?.toString() ?? 'DEBIT',
      transactionMethod: json['transactionMethod']?.toString() ?? 'ONLINE',
      transactionStatus: json['transactionStatus']?.toString() ?? 'PENDING',
      amount: (json['amount'] ?? 0).toDouble(),
      referenceId: json['referenceId']?.toString(),
      openingBalance: json['openingBalance']?.toDouble(),
      closingBalance: json['closingBalance']?.toDouble(),
      transactionDate: json['transactionDate'] != null
          ? DateTime.tryParse(json['transactionDate'].toString())
          : null,
      description: json['description']?.toString(),
      targetAccountId: json['targetAccountId']?.toString(),
    );
  }

  Map<String, dynamic> toJson() => {
        'amount': amount,
        'transactionType': transactionType,
        'description': description,
        'targetAccountId': targetAccountId,
      };

  bool get isCredit => transactionType == 'CREDIT';
  bool get isDebit => transactionType == 'DEBIT';
  bool get isSuccess => transactionStatus == 'SUCCESS';
  bool get isFailed => transactionStatus == 'FAILED';
  bool get isPending => transactionStatus == 'PENDING';
}
