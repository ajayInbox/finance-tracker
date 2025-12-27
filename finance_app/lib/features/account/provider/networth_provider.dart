import 'package:finance_app/features/account/data/model/networth_summary.dart';
import 'package:finance_app/features/account/data/providers/account_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final networthProvider = FutureProvider<NetworthSummary>((ref) async {
  final repo = ref.watch(accountRepositoryProvider);
  return repo.getNetWorth();
});
