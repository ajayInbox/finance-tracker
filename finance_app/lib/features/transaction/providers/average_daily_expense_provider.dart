import 'package:finance_app/features/transaction/data/model/average_daily_expense.dart';
import 'package:finance_app/features/transaction/data/providers/transaction_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final averageDailyExpenseProvider =
    FutureProvider<AverageDailyExpense>((ref) async {
  final repo = ref.watch(transactionRepositoryProvider);
  return repo.fetchAverageDailyExpense();
});
