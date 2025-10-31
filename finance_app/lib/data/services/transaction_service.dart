import 'package:another_telephony/telephony.dart';
import 'package:finance_app/data/models/expense_report.dart';
import 'package:finance_app/data/models/sms_message.dart';

import '../repository/transaction_repository.dart';
import '../models/transaction_summary.dart';
import '../models/transaction.dart';
import '../models/average_daily_expense.dart';

// application/service
class TransactionService {
  final TransactionRepository _repo = TransactionRepository();

  // Example orchestration: fetch and sort descending by date
  Future<List<TransactionSummary>> getFeed() async {
    try {
      final items = await _repo.fetchAllTransactions();
      return items;
    } catch (e) {
      // Return mock data if API fails
      throw Error();
    }
  }

  Future<void> addTransaction(Transaction transaction) async {
    await _repo.createTransaction(transaction);
  }

  Future<AverageDailyExpense> getAverageDailyExpense() async {
    try {
      return await _repo.fetchAverageDailyExpense();
    } catch (e) {
      // Return mock data if API fails
      throw Error();
    }
  }

  Future<ExpenseReport> fetchExpenseReport() async {
    try {
      return await _repo.fetchExpenseReport();
    } catch (e) {
      throw Error();
    }
  }

  Future<void> exportMessage(List<SmsMessage> smsMessages) async {
    try {
      List<SmsMessageObject> messages = smsMessages.map((msg) => SmsMessageObject(
        messageAddress: msg.address!,
        messageHeader: msg.address!,
        messageBody: msg.body!,
        messageDate: DateTime.fromMillisecondsSinceEpoch(msg.date!).toString()
      )).toList();

      await _repo.exportSmsMessages(messages);
    } catch (e) {
      throw Error();
    }
  }

  
}
