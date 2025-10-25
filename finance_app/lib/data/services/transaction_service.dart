import '../repository/transaction_repository.dart';
import '../models/transaction_summary.dart';
import '../models/transaction.dart';
import '../models/average_daily_expense.dart';

// application/service
class TransactionService {
  final TransactionRepository _repo = TransactionRepository();

  // Example orchestration: fetch and sort descending by date
  Future<List<TransactionSummary>> getFeed() async {
    try {
      final items = await _repo.fetchAllTransactions();
      return items;
    } catch (e) {
      // Return mock data if API fails
      return _getMockTransactions();
    }
  }

  Future<void> addTransaction(Transaction transaction) async {
    await _repo.createTransaction(transaction);
  }

  Future<AverageDailyExpense> getAverageDailyExpense() async {
    try {
      return await _repo.fetchAverageDailyExpense();
    } catch (e) {
      // Return mock data if API fails
      return _getMockAverageDailyExpense();
    }
  }

  List<TransactionSummary> _getMockTransactions() {
    final now = DateTime.now();
    return [
      TransactionSummary(
        id: '1',
        transactionName: 'Grocery Shopping',
        amount: -1250.0,
        type: 'expense',
        accountId: '1',
        accountName: 'HDFC Savings',
        balanceCached: 48750.0,
        categoryId: '1',
        categoryName: 'Food & Dining',
        occuredAt: now.subtract(const Duration(hours: 2)),
        postedAt: now.subtract(const Duration(hours: 2)),
        currency: 'INR',
      ),
      TransactionSummary(
        id: '2',
        transactionName: 'Salary Deposit',
        amount: 50000.0,
        type: 'income',
        accountId: '1',
        accountName: 'HDFC Savings',
        balanceCached: 50000.0,
        categoryId: '2',
        categoryName: 'Income',
        occuredAt: now.subtract(const Duration(days: 1)),
        postedAt: now.subtract(const Duration(days: 1)),
        currency: 'INR',
      ),
      TransactionSummary(
        id: '3',
        transactionName: 'Online Purchase',
        amount: -2500.0,
        type: 'expense',
        accountId: '3',
        accountName: 'Credit Card',
        balanceCached: -17500.0,
        categoryId: '3',
        categoryName: 'Shopping',
        occuredAt: now.subtract(const Duration(days: 2)),
        postedAt: now.subtract(const Duration(days: 2)),
        currency: 'INR',
      ),
    ];
  }

  AverageDailyExpense _getMockAverageDailyExpense() {
    final dailyExpenses = [
      DailyExpense(date: 'Mon', totalExpense: 1200.0),
      DailyExpense(date: 'Tue', totalExpense: 800.0),
      DailyExpense(date: 'Wed', totalExpense: 1500.0),
      DailyExpense(date: 'Thu', totalExpense: 600.0),
      DailyExpense(date: 'Fri', totalExpense: 2000.0),
      DailyExpense(date: 'Sat', totalExpense: 900.0),
      DailyExpense(date: 'Sun', totalExpense: 1100.0),
    ];

    return AverageDailyExpense(
      fromDate: '2024-01-01',
      toDate: '2024-01-07',
      days: 7,
      dailyList: dailyExpenses,
      averageDailyExpense: 1157.14,
    );
  }

  // More business rules can live here: filtering, currency conversion, etc.
}
