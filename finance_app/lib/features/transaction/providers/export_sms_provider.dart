import 'package:finance_app/features/transaction/data/model/sms_message.dart';
import 'package:finance_app/features/transaction/data/providers/transaction_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final exportSmsProvider =
    FutureProvider.family<void, List<SmsMessageObject>>((ref, messages) async {
  final repo = ref.read(transactionRepositoryProvider);
  await repo.exportSmsMessages(messages);
});
