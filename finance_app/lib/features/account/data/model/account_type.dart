enum AccountType {
  bank,
  savings,
  checking,
  cash,
  creditCard,
  loan,
  investment,
  unknown,
}

extension AccountTypeX on AccountType {
  String get label {
    switch (this) {
      case AccountType.bank:
        return "Bank Account";
      case AccountType.savings:
        return "Savings Account";
      case AccountType.checking:
        return "Checking Account";
      case AccountType.cash:
        return "Cash";
      case AccountType.creditCard:
        return "Credit Card";
      case AccountType.loan:
        return "Loan";
      case AccountType.investment:
        return "Investment";
      case AccountType.unknown:
        return "Unknown";
    }
  }

  String get apiValue {
    switch (this) {
      case AccountType.bank:
        return "BANK";
      case AccountType.savings:
        return "SAVINGS";
      case AccountType.checking:
        return "CHECKING";
      case AccountType.cash:
        return "CASH";
      case AccountType.creditCard:
        return "CREDIT_CARD";
      case AccountType.loan:
        return "LOAN";
      case AccountType.investment:
        return "INVESTMENT";
      case AccountType.unknown:
        return "UNKNOWN";
    }
  }
}

extension AccountTypeParser on String {
  AccountType toAccountType() {
    final normalized = trim().toUpperCase().replaceAll(" ", "_");

    switch (normalized) {
      case "BANK":
        return AccountType.bank;
      case "SAVINGS":
        return AccountType.savings;
      case "CHECKING":
        return AccountType.checking;
      case "CASH":
        return AccountType.cash;
      case "CREDIT_CARD":
        return AccountType.creditCard;
      case "LOAN":
        return AccountType.loan;
      case "INVESTMENT":
        return AccountType.investment;
      default:
        return AccountType.unknown;
    }
  }
}
