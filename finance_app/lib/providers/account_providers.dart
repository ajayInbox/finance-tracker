import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:finance_app/data/models/account.dart';
import 'package:finance_app/data/models/networth_summary.dart';
import 'package:finance_app/data/services/account_service.dart';

/// Service provider
final accountServiceProvider = Provider<AccountService>((ref) {
  return AccountService();
});

/// Accounts list provider
final accountsProvider = FutureProvider<List<Account>>((ref) async {
  final service = ref.watch(accountServiceProvider);
  return service.getAccounts();
});

/// Net worth provider
final networthProvider = FutureProvider<NetworthSummary>((ref) async {
  final service = ref.watch(accountServiceProvider);
  return service.getNetWorth();
});
