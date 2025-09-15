import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/transaction_summary.dart';

// domain/repositories
abstract class TransactionRepository {
  Future<List<TransactionSummary>> fetchAll();
}

// data/repositories

class HttpTransactionRepository implements TransactionRepository {
  HttpTransactionRepository({required this.baseUrl});
  final Uri baseUrl;

  @override
  Future<List<TransactionSummary>> fetchAll() async {
    final uri = baseUrl.replace(
      path: '/api/v1/transactions',
      queryParameters: {"version":"2"}
    );

    final res = await http.get(
      uri,
      headers: {'Accept': 'application/json'},
    );

    if (res.statusCode != 200) {
      throw Exception('Failed to load transactions: ${res.statusCode}');
    }

    final body = jsonDecode(res.body);
    final List list = body is List ? body : (body['content'] as List);
    return list
        .cast<Map<String, dynamic>>()
        .map(TransactionSummary.fromJson)
        .toList();
  }
}
