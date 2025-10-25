import 'dart:convert';
import 'package:finance_app/data/models/expense_report.dart';
import 'package:http/http.dart' as http;
import '../models/transaction_summary.dart';
import '../models/transaction.dart';
import '../models/average_daily_expense.dart';
import '../../utils/api_constants.dart';

// data/repositories
class TransactionRepository {

  Future<List<TransactionSummary>> fetchAllTransactions() async {

    Uri uri = Uri.parse(ApiConstants.baseUrl).replace(
      path: ApiConstants.getTransactions,
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

  Future<void> createTransaction(Transaction transaction) async {
    Uri uri = Uri.parse(ApiConstants.baseUrl).replace(
      path: ApiConstants.createTransaction,
    );

    final res = await http.post(
      uri,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: jsonEncode(transaction.toJson()),
    );

    if (res.statusCode != 201 && res.statusCode != 200) {
      throw Exception('Failed to create transaction: ${res.statusCode} - ${res.body}');
    }
  }

  Future<AverageDailyExpense> fetchAverageDailyExpense() async {
    Uri uri = Uri.parse(ApiConstants.baseUrl).replace(
      path: ApiConstants.avgDaily,
    );

    final res = await http.post(
      uri,
      headers: {
        'Content-Type': 'application/json',
        'Accept': 'application/json'
      },
      body: jsonEncode(Map.of({"fromDate":null, "toDate":null, "account": null, "category":null, "query":null})),
    );

    if (res.statusCode != 200) {
      throw Exception('Failed to load average daily expense: ${res.statusCode}');
    }

    final body = jsonDecode(res.body);
    return AverageDailyExpense.fromJson(body);
  }

  Future<ExpenseReport> fetchExpenseReport() async {
    Uri uri = Uri.parse(ApiConstants.baseUrl).replace(
      path: ApiConstants.expenseReport,
      queryParameters: {"duration":null}
    );

    final res = await http.get(
      uri,
      headers: {'Accept': 'application/json'},
    );

    if (res.statusCode != 200) {
      throw Exception('Failed to load average daily expense: ${res.statusCode}');
    }

    final body = jsonDecode(res.body);
    return ExpenseReport.fromJson(body);
  }
}
