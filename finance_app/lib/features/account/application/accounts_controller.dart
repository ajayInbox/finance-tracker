import 'dart:async';
import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/account/data/model/account_create_update_request.dart';
import 'package:finance_app/features/account/data/providers/account_repository_provider.dart';
import 'package:finance_app/features/account/provider/networth_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final accountsControllerProvider =
    AsyncNotifierProvider<AccountsController, List<Account>>(
      AccountsController.new,
    );

class AccountsController extends AsyncNotifier<List<Account>> {
  @override
  FutureOr<List<Account>> build() {
    final repo = ref.watch(accountRepositoryProvider);
    return repo.fetchAllAccounts();
  }

  // ---------------------------------------------------------------------------
  // REFRESH (SAFE)
  // ---------------------------------------------------------------------------

  Future<void> refresh() async {
    state = await AsyncValue.guard(() async {
      final repo = ref.read(accountRepositoryProvider);
      return repo.fetchAllAccounts();
    });
  }

  // ---------------------------------------------------------------------------
  // CREATE
  // ---------------------------------------------------------------------------

  Future<void> createAccount(AccountCreateUpdateRequest account) async {
    await ref.read(accountRepositoryProvider).createAccount(account);
    await refresh();
    _invalidateDerivedProviders();
  }

  // ---------------------------------------------------------------------------
  // DELETE
  // ---------------------------------------------------------------------------

  Future<void> deleteAccount(String id) async {
    await ref.read(accountRepositoryProvider).deleteAccount(id);
    await refresh();
    _invalidateDerivedProviders();
  }

  // ---------------------------------------------------------------------------
  // SIDE EFFECTS
  // ---------------------------------------------------------------------------

  void _invalidateDerivedProviders() {
    ref.invalidate(networthProvider);
  }
}
