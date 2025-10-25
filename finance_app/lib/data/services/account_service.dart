import 'package:finance_app/data/models/account.dart';
import 'package:finance_app/data/repository/account_repository.dart';

class AccountService {

  final AccountRepository _repo = AccountRepository();

  Future<List<Account>> getAccounts() async {
    try {
      return await _repo.fetchAllAccounts();
    } catch (e) {
      // Return mock data if API fails
      return _getMockAccounts();
    }
  }

  List<Account> _getMockAccounts() {
    return [
      Account(
        id: '1',
        accountName: 'HDFC Savings',
        accountType: 'SAVINGS',
        balance: 50000.0,
        currency: 'INR',
        isActive: true,
        createdAt: DateTime.now().subtract(const Duration(days: 30)),
        updatedAt: DateTime.now(),
      ),
      Account(
        id: '2',
        accountName: 'SBI Checking',
        accountType: 'CHECKING',
        balance: 25000.0,
        currency: 'INR',
        isActive: true,
        createdAt: DateTime.now().subtract(const Duration(days: 15)),
        updatedAt: DateTime.now(),
      ),
      Account(
        id: '3',
        accountName: 'Credit Card',
        accountType: 'CREDIT_CARD',
        balance: -15000.0,
        currency: 'INR',
        isActive: true,
        createdAt: DateTime.now().subtract(const Duration(days: 60)),
        updatedAt: DateTime.now(),
      ),
      Account(
        id: '4',
        accountName: 'Cash Wallet',
        accountType: 'CASH',
        balance: 5000.0,
        currency: 'INR',
        isActive: true,
        createdAt: DateTime.now().subtract(const Duration(days: 7)),
        updatedAt: DateTime.now(),
      ),
      Account(
        id: '5',
        accountName: 'Investment Portfolio',
        accountType: 'INVESTMENT',
        balance: 100000.0,
        currency: 'INR',
        isActive: true,
        createdAt: DateTime.now().subtract(const Duration(days: 90)),
        updatedAt: DateTime.now(),
      ),
    ];
  }
}
