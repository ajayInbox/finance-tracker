
class TransactionSummary {

  late final String id;
  late final String transactionName;
  final double amount;
  final String type;
  final String accountId;
  final String accountName;
  final double balanceCached;
  final String categoryId;

  final String categoryName;
  final DateTime occurredAt;
  final DateTime postedAt;
  final String currency;

    TransactionSummary({required this.id, 
      required this.transactionName, 
      required this.amount, 
      required this.type, 
      required this.accountId, 
      required this.accountName, 
      required this.balanceCached, 
      required this.categoryId, 
      required this.categoryName, 
      required this.occurredAt, 
      required this.postedAt, 
      required this.currency,});


  factory TransactionSummary.fromJson(Map<String, dynamic> j) => TransactionSummary(
        id: j['id'] as String,
        transactionName: (j['transactionName']??'name') as String,
        type: j['type'] as String,
        amount: j['amount'] as double,
        accountId: j['accountId'] as String,
        categoryId: j['categoryId'] as String,
        accountName: (j['accountName'] ?? '') as String,
        balanceCached: (j['balanceCached'] ?? 0.0) as double,
        categoryName: (j['categoryName'] ?? "") as String,
        occurredAt: (DateTime.parse(j['occurredAt'])),
        postedAt: (DateTime.parse(j['occurredAt'])),
        currency: (j['currency'] ?? 'INR') as String,
  );




}