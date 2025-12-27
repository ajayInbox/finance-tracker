import 'package:finance_app/features/account/data/model/account_create_update_request.dart';
import 'package:finance_app/features/account/data/providers/account_repository_provider.dart';
import 'package:finance_app/features/account/provider/accounts_provider.dart';
import 'package:finance_app/features/account/provider/networth_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final createAccountProvider =
    FutureProvider.family<void, AccountCreateUpdateRequest>((ref, request) async {
  final repo = ref.read(accountRepositoryProvider);
  await repo.createAccount(request);

  // auto-refresh after creation
  ref.invalidate(accountsProvider);
  ref.invalidate(networthProvider);
});
