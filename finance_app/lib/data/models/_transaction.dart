

class Transaction {

  final String id;
  final String transactionName;
  final double amount;
  final String type;
  final String merchant;
  final String notes;
  final String account;
  final String category;

  final List tags;
  final DateTime occuredAt;
  final DateTime postedAt;
  final String currency;
  final String attachments;
  final String externalRef;

    Transaction({required this.id, 
      required this.transactionName, 
      required this.amount, 
      required this.type, 
      required this.merchant, 
      required this.notes, 
      required this.account, 
      required this.category, 
      required this.tags, 
      required this.occuredAt, 
      required this.postedAt, 
      required this.currency, 
      required this.attachments, 
      required this.externalRef});


  factory Transaction.fromJson(Map<String, dynamic> j) => Transaction(
        id: j['_id'] as String,
        transactionName: (j['transactionName']??'name') as String,
        type: j['type'] as String,
        amount: j['amount'] as double,
        account: j['account'] as String,
        category: j['category'] as String,
        merchant: (j['merchant'] ?? '') as String,
        notes: (j['notes'] ?? '') as String,
        tags: (j['tags'] ?? List.empty()) as List,
        occuredAt: (DateTime.now()),
        postedAt: (DateTime.now()),
        currency: (j['currency'] ?? 'INR') as String,
        attachments: (j['attachments'] ?? '') as String,
        externalRef: (j['externalRef'] ?? '') as String,
  );




}