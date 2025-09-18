
class Account {

  final String id;
  final String accountName;

  Account({required this.id, required this.accountName});

  factory Account.fromJson(Map<String, dynamic> j) => Account (
    id: j['id'] as String,
    accountName: j['label'] as String
  );
}