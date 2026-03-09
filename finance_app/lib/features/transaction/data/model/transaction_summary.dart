class TransactionSummary {
  final String id;
  final String transactionName;
  final double amount;
  final String type;
  final String accountName;
  final String categoryName;
  final DateTime occurredAt;
  final String currency;

  TransactionSummary({
    required this.id,
    required this.transactionName,
    required this.amount,
    required this.type,
    required this.accountName,
    required this.categoryName,
    required this.occurredAt,
    required this.currency,
  });

  factory TransactionSummary.fromJson(Map<String, dynamic> j) =>
      TransactionSummary(
        id: j['id'] as String,
        transactionName: (j['transactionName'] ?? 'name') as String,
        type: j['type'] as String,
        amount: j['amount'] as double,
        accountName: (j['accountName'] ?? '') as String,
        categoryName: (j['categoryName'] ?? "") as String,
        occurredAt: (DateTime.parse(j['occurredAt'])),
        currency: (j['currency'] ?? 'INR') as String,
      );
}
