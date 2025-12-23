import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:finance_app/features/transaction/data/model/average_daily_expense.dart';
import 'package:finance_app/features/transaction/data/model/expense_report.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';
import 'package:finance_app/data/services/transaction_service.dart';

/// Service provider
final transactionServiceProvider = Provider<TransactionService>((ref) {
  return TransactionService();
});

/// Transactions feed provider
final transactionsProvider = FutureProvider<List<TransactionSummary>>((ref) async {
  final service = ref.watch(transactionServiceProvider);
  return service.getFeed();
});

/// Expense report provider
final expenseReportProvider = FutureProvider<ExpenseReport>((ref) async {
  final service = ref.watch(transactionServiceProvider);
  return service.fetchExpenseReport();
});

/// Average daily expense provider
final averageDailyExpenseProvider = FutureProvider<AverageDailyExpense>((ref) async {
  final service = ref.watch(transactionServiceProvider);
  return service.getAverageDailyExpense();
});
