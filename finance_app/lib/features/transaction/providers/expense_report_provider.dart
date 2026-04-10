import 'package:finance_app/features/transaction/data/model/expense_report.dart';
import 'package:finance_app/features/transaction/data/providers/transaction_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class ExpenseReportPeriodNotifier extends Notifier<String> {
  @override
  String build() => '1M';

  void setPeriod(String period) => state = period;
}

final expenseReportPeriodProvider =
    NotifierProvider<ExpenseReportPeriodNotifier, String>(
      ExpenseReportPeriodNotifier.new,
    );

final expenseReportProvider = FutureProvider<ExpenseReport>((ref) async {
  final repo = ref.watch(transactionRepositoryProvider);
  final period = ref.watch(expenseReportPeriodProvider);

  final now = DateTime.now();
  DateTime start;
  DateTime end = now;

  switch (period) {
    case '1W':
      start = now.subtract(const Duration(days: 7));
      break;
    case '1M':
      // 1st day of the current month with 0:0:0 time
      start = DateTime(now.year, now.month, 1);
      break;
    case '3M':
      start = DateTime(now.year, now.month - 2, 1);
      break;
    case '6M':
      start = DateTime(now.year, now.month - 5, 1);
      break;
    case '1Y':
      start = DateTime(now.year, now.month - 11, 1);
      break;
    default:
      start = DateTime(now.year, now.month, 1);
  }

  return repo.fetchExpenseReport(start: start, end: end, type: 'EXPENSE');
});
