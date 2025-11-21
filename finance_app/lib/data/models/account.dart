import 'account_type.dart';

class Account {
  final String id;
  final String accountName;
  final AccountType accountType;
  final String? lastFour;
  final String currency;
  final DateTime? openingDate;
  final double? startingBalance;
  final double? currentOutstanding;
  final int? cutoffDayOfMonth;
  final int? dueDayOfMonth;
  final double? creditLimit;
  final double? currentBalance;
  final bool active;
  final bool readOnly;
  final DateTime createdAt;
  final DateTime? closedAt;
  final String? notes;
  final bool isAsset;     // ASSET / LIABILITY converted to bool

  Account({
    required this.id,
    required this.accountName,
    required this.accountType,
    this.lastFour,
    required this.currency,
    this.openingDate,
    this.startingBalance,
    this.currentOutstanding,
    this.cutoffDayOfMonth,
    this.dueDayOfMonth,
    this.creditLimit,
    this.currentBalance,
    required this.active,
    required this.readOnly,
    required this.createdAt,
    this.closedAt,
    this.notes,
    required this.isAsset,
  });

  factory Account.fromJson(Map<String, dynamic> j) {
    return Account(
      id: j['id'],
      accountName: j['accountName'] ?? 'Unnamed Account',
      accountType: (j['accountType'] as String?)?.toAccountType() ?? AccountType.unknown,
      lastFour: j['lastFour'],
      currency: j['currency'] ?? 'INR',

      openingDate: j['openingDate'] != null
          ? DateTime.tryParse(j['openingDate'])
          : null,

      startingBalance: _toDouble(j['startingBalance']),
      currentOutstanding: _toDouble(j['currentOutstanding']),
      cutoffDayOfMonth: j['cutoffDayOfMonth'],
      dueDayOfMonth: j['dueDayOfMonth'],
      creditLimit: _toDouble(j['creditLimit']),
      currentBalance: _toDouble(j['currentBalance']),

      active: j['active'] ?? true,
      readOnly: j['readOnly'] ?? false,

      createdAt: DateTime.parse(j['createdAt']),
      closedAt:
      j['closedAt'] != null ? DateTime.tryParse(j['closedAt']) : null,

      notes: j['notes'],

      isAsset: (j['category'] ?? 'ASSET').toUpperCase() == 'ASSET',
    );
  }

  double get effectiveBalance {
    if (!isAsset) {
      return currentOutstanding ?? 0.0;
    }
    return currentBalance ?? startingBalance ?? 0.0;
  }

}

double? _toDouble(dynamic v) {
  if (v == null) return null;
  if (v is double) return v;
  if (v is int) return v.toDouble();
  return double.tryParse(v.toString());
}
