import 'package:finance_app/core/dio_provider.dart';
import 'package:finance_app/features/account/data/repository/account_repository.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final accountRepositoryProvider = Provider<AccountRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return AccountRepository(dio);
});
