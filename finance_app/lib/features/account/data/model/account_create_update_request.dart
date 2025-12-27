class AccountCreateUpdateRequest {
  final String accountName;
  final int lastFour;
  final String accountType;
  final DateTime openingDate;
  final double startingBalance;
  final double currentOutstanding;
  final String currency;
  final double creditLimit;
  final String cutOffDay;
  final String dueDate;
  final String notes;
  final bool hideFromSelection;
  final bool hideFromReports;
  final String category; // 'asset' or 'liability'

  AccountCreateUpdateRequest({
    required this.accountName,
    required this.lastFour,
    required this.accountType,
    required this.openingDate,
    required this.startingBalance,
    required this.currentOutstanding,
    required this.currency,
    required this.creditLimit,
    required this.cutOffDay,
    required this.dueDate,
    required this.notes,
    required this.hideFromSelection,
    required this.hideFromReports,
    required this.category,
  });

  Map<String, dynamic> toJson() {
    return {
      'accountName': accountName,
      'lastFour': lastFour,
      'accountType': accountType,
      'openingDate': openingDate.toIso8601String(),
      'startingBalance': startingBalance,
      'currentOutstanding': currentOutstanding,
      'currency': currency,
      'creditLimit': creditLimit,
      'cutOffDay': cutOffDay,
      'dueDate': dueDate,
      'notes': notes,
      'hideFromSelection': hideFromSelection,
      'hideFromReports': hideFromReports,
      'category': category,
    };
  }
}
