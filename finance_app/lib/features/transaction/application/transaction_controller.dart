import 'package:finance_app/features/account/provider/accounts_provider.dart';
import 'package:finance_app/features/account/provider/networth_provider.dart';
import 'package:finance_app/features/transaction/data/model/transaction.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';
import 'package:finance_app/features/transaction/data/providers/transaction_repository_provider.dart';
import 'package:finance_app/features/transaction/providers/average_daily_expense_provider.dart';
import 'package:finance_app/features/transaction/providers/expense_report_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final transactionsControllerProvider =
    AsyncNotifierProvider<TransactionsController, List<TransactionSummary>>(
  TransactionsController.new,
);

class TransactionsController
    extends AsyncNotifier<List<TransactionSummary>> {

  @override
  Future<List<TransactionSummary>> build() async {
    final repo = ref.read(transactionRepositoryProvider);
    return repo.fetchAllTransactions();
  }

  // ---------------------------------------------------------------------------
  // REFRESH (SAFE)
  // ---------------------------------------------------------------------------

  Future<void> refresh() async {
    state = await AsyncValue.guard(() async {
      final repo = ref.read(transactionRepositoryProvider);
      return repo.fetchAllTransactions();
    });
  }

  // ---------------------------------------------------------------------------
  // DELETE (OPTIMISTIC + NULL SAFE)
  // ---------------------------------------------------------------------------

  Future<void> deleteTransaction(String id) async {
    final current = state.value;
    if (current == null) return; // ✅ critical guard

    final removed = current.firstWhere((t) => t.id == id);

    // optimistic remove
    state = AsyncData(current.where((t) => t.id != id).toList());

    try {
      await ref.read(transactionRepositoryProvider).deleteTransaction(id);
      _invalidateDerivedProviders();
    } catch (e) {
      // rollback
      state = AsyncData([...current, removed]);
      rethrow;
    }
  }

  // ---------------------------------------------------------------------------
  // CREATE
  // ---------------------------------------------------------------------------

  Future<void> createTransaction(Transaction tx) async {
    await ref.read(transactionRepositoryProvider).createTransaction(tx);
    await refresh();
    _invalidateDerivedProviders();
  }

  // ---------------------------------------------------------------------------
  // UPDATE
  // ---------------------------------------------------------------------------

  Future<void> updateTransaction(String id, Transaction tx) async {
    await ref.read(transactionRepositoryProvider).updateTransaction(id, tx);
    await refresh();
    _invalidateDerivedProviders();
  }

  // ---------------------------------------------------------------------------
  // SIDE EFFECTS
  // ---------------------------------------------------------------------------

  void _invalidateDerivedProviders() {
    ref.invalidate(averageDailyExpenseProvider);
    ref.invalidate(expenseReportProvider);
    ref.invalidate(accountsProvider);
    ref.invalidate(networthProvider);
  }
}
