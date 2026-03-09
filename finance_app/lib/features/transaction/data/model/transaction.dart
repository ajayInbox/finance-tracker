class Transaction {
  final String transactionName;
  final double amount;
  final String type;
  final String accountId;
  final String categoryId;
  final DateTime occurredAt;
  String currency;
  final String notes;

  Transaction({
    required this.transactionName,
    required this.amount,
    required this.type,
    required this.accountId,
    required this.categoryId,
    required this.occurredAt,
    this.currency = 'INR',
    this.notes = '',
  });

  Map<String, dynamic> toJson() {
    return {
      'transactionName': transactionName,
      'amount': amount,
      'type': type,
      'accountId': accountId,
      'categoryId': categoryId,
      'occurredAt': occurredAt.toIso8601String(),
      'notes': notes,
      'currency': currency,
    };
  }
}
