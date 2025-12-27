import 'package:dio/dio.dart';
import 'package:finance_app/features/transaction/data/model/average_daily_expense.dart';
import 'package:finance_app/features/transaction/data/model/expense_report.dart';
import 'package:finance_app/features/transaction/data/model/sms_message.dart';
import 'package:finance_app/features/transaction/data/model/transaction.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';
import 'package:finance_app/utils/api_constants.dart';

class TransactionRepository {
  final Dio dio;

  TransactionRepository(this.dio);

  Future<List<TransactionSummary>> fetchAllTransactions() async {
    final res = await dio.get(
      ApiConstants.getTransactions,
      queryParameters: {'version': '2'},
    );

    final data = res.data;
    final List list = data is List ? data : data['content'];

    return list
        .cast<Map<String, dynamic>>()
        .map(TransactionSummary.fromJson)
        .toList();
  }

  Future<void> createTransaction(Transaction transaction) async {
    await dio.post(
      ApiConstants.createTransaction,
      data: transaction.toJson(),
    );
  }

  Future<void> updateTransaction(
      String transactionId,
      Transaction transaction,
      ) async {
    await dio.put(
      '${ApiConstants.updateTransaction}/$transactionId',
      data: transaction.toJson(),
    );
  }

  Future<void> deleteTransaction(String transactionId) async {
    await dio.delete(
      '${ApiConstants.deleteTransaction}/$transactionId',
    );
  }

  Future<AverageDailyExpense> fetchAverageDailyExpense() async {
    final res = await dio.post(
      ApiConstants.avgDaily,
      data: {
        "fromDate": null,
        "toDate": null,
        "account": null,
        "category": null,
        "query": null,
      },
    );

    return AverageDailyExpense.fromJson(res.data);
  }

  Future<ExpenseReport> fetchExpenseReport() async {
    final res = await dio.get(
      ApiConstants.expenseReport,
      queryParameters: {'duration': null},
    );

    return ExpenseReport.fromJson(res.data);
  }

  Future<void> exportSmsMessages(List<SmsMessageObject> smsMessages) async {
    await dio.post(
      ApiConstants.exportMessages,
      data: smsMessages.map((e) => e.toJson()).toList(),
    );
  }
}
