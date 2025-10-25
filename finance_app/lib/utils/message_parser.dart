import 'dart:math';

class ParsedTransaction {
  final double? amount;
  final String? merchant;
  final String? categoryHint;
  final DateTime? date;

  ParsedTransaction({
    this.amount,
    this.merchant,
    this.categoryHint,
    this.date,
  });

  bool get isValid => amount != null && amount! > 0;
}

class MessageParser {
  // Regex patterns for common Indian SMS formats
  static final RegExp _amountRegex =
      RegExp(r'(?:₹|Rs\.?|INR)\s*([\d,]+\.?\d*)', caseSensitive: false);

  static final RegExp _debitRegex =
      RegExp(r'(?:debited|paid|charged|transferred)', caseSensitive: false);

  static final RegExp _creditRegex =
      RegExp(r'(?:credited|received|refunded)', caseSensitive: false);

  static final RegExp _merchantRegex =
      RegExp(r'(?:at|to|from)\s+([A-Za-z\s]+?)(?:via|on|through|by|\.|$)', caseSensitive: false);

  // Common category hints
  static const Map<String, String> _categoryMappings = {
    'amazon': 'Shopping',
    'flipkart': 'Shopping',
    'swiggy': 'Food',
    'zomato': 'Food',
    'uber': 'Transport',
    'ola': 'Transport',
    'irctc': 'Travel',
    'makemytrip': 'Travel',
    'netflix': 'Entertainment',
    'hotstar': 'Entertainment',
    'electricity': 'Bills',
    'water': 'Bills',
    'gas': 'Bills',
    'phone': 'Bills',
    'mobile': 'Bills',
    'airtel': 'Bills',
    'vodafone': 'Bills',
    'jio': 'Bills',
  };

  ParsedTransaction parse(String message) {
    message = message.toLowerCase();

    double? amount = _extractAmount(message);
    String? merchant = _extractMerchant(message);
    String? categoryHint = merchant != null ? _guessCategory(merchant) : null;
    String transactionType = _debitRegex.hasMatch(message) ? 'Expense' :
                           _creditRegex.hasMatch(message) ? 'Income' : 'Expense'; // Default to expense

    // For now, assume date is today; in future could parse date from message
    DateTime? date = DateTime.now();

    // Override categoryHint based on transaction type; if not expense, clear category
    if (transactionType != 'Expense') {
      categoryHint = null;
    }

    return ParsedTransaction(
      amount: amount,
      merchant: merchant,
      categoryHint: categoryHint,
      date: date,
    );
  }

  double? _extractAmount(String message) {
    final match = _amountRegex.firstMatch(message);
    if (match != null) {
      String amountStr = match.group(1)?.replaceAll(',', '') ?? '';
      return double.tryParse(amountStr);
    }
    return null;
  }

  String? _extractMerchant(String message) {
    final match = _merchantRegex.firstMatch(message);
    if (match != null) {
      String merchant = match.group(1)?.trim() ?? '';
      // Clean up common prefixes
      merchant = merchant.replaceAll(RegExp(r'^(the|a|an)\s+', caseSensitive: false), '');
      return merchant.isNotEmpty ? merchant : null;
    }
    return null;
  }

  String? _guessCategory(String merchant) {
    String cleanMerchant = merchant.toLowerCase();
    for (var entry in _categoryMappings.entries) {
      if (cleanMerchant.contains(entry.key)) {
        return entry.value;
      }
    }
    return null;
  }
}
