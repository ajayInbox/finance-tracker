
import 'package:finance_app/data/models/account.dart';
import 'package:finance_app/data/models/networth_summary.dart';
import '../../utils/api_constants.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class AccountRepository {

  Future<List<Account>> fetchAllAccounts() async {
    Uri uri = Uri.parse(ApiConstants.baseUrl).replace(
      path: ApiConstants.getAccounts,
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
        .map(Account.fromJson)
        .toList();
  }

  Future<NetworthSummary> getNetWorth() async {
    Uri uri = Uri.parse(ApiConstants.baseUrl).replace(
      path: ApiConstants.networthSummary,
      queryParameters: {'userId': 'ABC'}
    );

    final res = await http.get(
      uri,
      headers: {'Accept': 'application/json'},
    );

    if (res.statusCode != 200) {
      throw Exception('Failed to load transactions: ${res.statusCode}');
    }

    final body = jsonDecode(res.body);
    return NetworthSummary.fromJson(body);
  }
}