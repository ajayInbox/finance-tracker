import 'package:finance_app/core/dio_provider.dart';
import 'package:finance_app/features/transaction/data/repository/transaction_repository.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final transactionRepositoryProvider =
    Provider<TransactionRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return TransactionRepository(dio);
});
