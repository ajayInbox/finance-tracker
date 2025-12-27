import 'package:finance_app/features/transaction/data/model/expense_report.dart';
import 'package:finance_app/features/transaction/data/providers/transaction_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final expenseReportProvider =
    FutureProvider<ExpenseReport>((ref) async {
  final repo = ref.watch(transactionRepositoryProvider);
  return repo.fetchExpenseReport();
});
