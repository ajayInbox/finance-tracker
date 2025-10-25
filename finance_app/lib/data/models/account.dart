
class Account {
  final String id;
  final String accountName;
  final String? accountType; // e.g., "SAVINGS", "CREDIT_CARD", "CASH", etc.
  final double? balance;
  final String? currency;
  final bool? isActive;
  final DateTime? createdAt;
  final DateTime? updatedAt;

  Account({
    required this.id,
    required this.accountName,
    this.accountType,
    this.balance,
    this.currency,
    this.isActive,
    this.createdAt,
    this.updatedAt,
  });

  factory Account.fromJson(Map<String, dynamic> j) => Account(
    id: j['id'] as String,
    accountName: j['label'] as String? ?? j['accountName'] as String? ?? 'Unknown Account',
    accountType: j['type'] as String?,
    balance: j['balanceCached']?.toDouble() ?? j['balance']?.toDouble(),
    currency: j['currency'] as String?,
    isActive: j['isActive'] as bool?,
    createdAt: j['createdAt'] != null ? DateTime.parse(j['createdAt']) : null,
    updatedAt: j['updatedAt'] != null ? DateTime.parse(j['updatedAt']) : null,
  );

  // Helper method to determine if account is an asset or liability
  bool get isAsset {
    if (accountType == null) return true; // Default to asset

    final type = accountType!.toUpperCase();
    // Credit cards and loans are typically liabilities
    return !type.contains('CARD') && !type.contains('LOAN') && !type.contains('LIABILITY');
  }

  // Helper method to get display balance
  double get displayBalance {
    return balance ?? 0.0;
  }

  // Helper method to get account type for display
  String get displayType {
    if (accountType == null) return 'Account';

    final type = accountType!.toUpperCase();
    if (type.contains('SAVINGS')) return 'Savings Account';
    if (type.contains('CHECKING')) return 'Checking Account';
    if (type.contains('CREDIT')) return 'Credit Card';
    if (type.contains('CASH')) return 'Cash';
    if (type.contains('INVESTMENT')) return 'Investment';
    if (type.contains('LOAN')) return 'Loan';
    return accountType!;
  }
}
