import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/account/data/providers/account_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final accountsProvider = FutureProvider<List<Account>>((ref) async {
  final repo = ref.watch(accountRepositoryProvider);
  return repo.fetchAllAccounts();
});
