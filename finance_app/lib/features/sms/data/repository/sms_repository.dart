import 'package:dio/dio.dart';
import 'package:finance_app/core/dio_provider.dart';
import 'package:finance_app/features/sms/data/model/transaction_draft.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final smsRepositoryProvider = Provider<SmsRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return SmsRepository(dio);
});

class SmsRepository {
  final Dio _dio;
  SmsRepository(this._dio);

  Future<List<TransactionDraft>> fetchDrafts() async {
    try {
      final response = await _dio.get(
        '/api/v1/transactions',
        queryParameters: {'version': 3},
      );
      final rawData = response.data as List<dynamic>;
      return rawData
          .map((e) => TransactionDraft.fromJson(e as Map<String, dynamic>))
          .toList();
    } catch (e) {
      // In case of error (e.g. backend not running), rethrow or fallback?
      // For now, adhering to user request to fetch from backend.
      // If we wanted fallback, we could do:
      // return _fetchDummyDrafts();
      throw Exception('Failed to fetch drafts: $e');
    }
  }
}
