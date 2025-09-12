import 'dart:convert';
import 'package:http/http.dart' as http;
import '../models/_transaction.dart';

// domain/repositories
abstract class TransactionRepository {
  Future<List<Transaction>> fetchAll();
}

// data/repositories

class HttpTransactionRepository implements TransactionRepository {
  HttpTransactionRepository({required this.baseUrl});
  final Uri baseUrl;

  @override
  Future<List<Transaction>> fetchAll() async {
    final uri = baseUrl.replace(
      path: '/api/v1/transactions',
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
        .map(Transaction.fromJson)
        .toList();
  }
}
