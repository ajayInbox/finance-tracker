class Transaction {
  final String transactionName;
  final double amount;
  final String type;
  final String account;
  final String category;
  final DateTime occurredAt;
  String currency;
  final String notes;

  Transaction({
    required this.transactionName,
    required this.amount,
    required this.type,
    required this.account,
    required this.category,
    required this.occurredAt,
    this.currency = 'INR',
    this.notes = '',
  });

  Map<String, dynamic> toJson() {
    return {
      'transactionName': transactionName,
      'amount': amount,
      'type': type,
      'account': account,
      'category': category,
      'occurredAt': occurredAt.toIso8601String(),
      'notes': notes,
      'currency': currency,
    };
  }
}
