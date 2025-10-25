class Transaction {
  final String transactionName;
  final double amount;
  final String type;
  final String account;
  final String category;
  final DateTime occuredAt;
  final String notes;

  Transaction({
    required this.transactionName,
    required this.amount,
    required this.type,
    required this.account,
    required this.category,
    required this.occuredAt,
    this.notes = '',
  });

  Map<String, dynamic> toJson() {
    return {
      'transactionName': transactionName,
      'amount': amount,
      'type': type,
      'account': account,
      'category': category,
      'occuredAt': occuredAt.toIso8601String(),
      'notes': notes,
    };
  }
}
