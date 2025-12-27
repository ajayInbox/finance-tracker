class NetworthSummary {
  final ValueNumber assets;
  final ValueNumber liabilities;
  final double netWorth;

  NetworthSummary({
    required this.assets,
    required this.liabilities,
    required this.netWorth,
  });

  factory NetworthSummary.fromJson(Map<String, dynamic> json) {
    return NetworthSummary(
      assets: (ValueNumber.fromJson(json['assets'])),
      liabilities: (ValueNumber.fromJson(json['liabilities'])),
      netWorth: (json['netWorth'] ?? 0).toDouble(),
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'totalAssets': assets,
      'totalLiabilities': liabilities,
      'netWorth': netWorth,
    };
  }

  // Helper method to format currency display
  String formatCurrency(double amount) {
    return '₹${amount.toStringAsFixed(2)}';
  }

  // Display formatted values
  String get formattedAssets => formatCurrency(assets.total);
  String get formattedLiabilities => formatCurrency(liabilities.total);
  String get formattedNetWorth => formatCurrency(netWorth);

  // Check if user has positive net worth
  bool get isPositiveNetWorth => netWorth > 0;
}

class ValueNumber {

  final double total;
  final int number;

  ValueNumber({
    required this.total,
    required this.number
  });

  factory ValueNumber.fromJson(Map<String, dynamic> json) {
    return ValueNumber(
      total: (json['total'] ?? 0).toDouble(),
      number: (json['number'] ?? 0)
    );
  }
}
