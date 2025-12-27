// import 'package:finance_app/features/account/data/model/account.dart';
// import 'package:finance_app/features/category/data/models/category.dart';
// import 'package:finance_app/features/transaction/data/model/transaction.dart';
// import 'package:finance_app/utils/message_parser.dart';
// import 'package:shared_preferences/shared_preferences.dart';
// import 'package:another_telephony/telephony.dart';
// import 'package:permission_handler/permission_handler.dart';

// class SmsTransactionSyncService {
//   static const String _lastScanTimestampKey = 'last_sms_scan_timestamp';

//   Future<void> scanForNewTransactions() async {
//     final status = await Permission.sms.status;
//     if (!status.isGranted) return;

//     final prefs = await SharedPreferences.getInstance();
//     final lastScannedTimestamp = prefs.getInt(_lastScanTimestampKey) ?? 0;

//     final newMessages = await _getSmsAfter(lastScannedTimestamp);
//     if (newMessages.isNotEmpty) {
//       await _autoAddTransactions(newMessages);
//       await prefs.setInt(_lastScanTimestampKey, DateTime.now().millisecondsSinceEpoch);
//     }
//   }

//   Future<List<SmsMessage>> _getSmsAfter(int timestamp) async {
//     final telephony = Telephony.instance;
//     final messages = await telephony.getInboxSms(
//       sortOrder: [OrderBy(SmsColumn.DATE, sort: Sort.DESC)],
//     );
//     // Filter messages newer than timestamp
//     return messages.where((msg) =>
//       msg.date != null && msg.date! > timestamp
//     ).toList();
//   }

//   Future<void> _autoAddTransactions(List<SmsMessage> messages) async {
//     try {
//       final accounts = await AccountService().getAccounts();
//       final categories = await CategoryService().getAllCategories();
//       if (accounts.isEmpty || categories.isEmpty) return; // No accounts or cats available

//       for (var msg in messages) {
//         final parsed = MessageParser().parse(msg.body ?? '');
//         if (parsed.isValid) {
//           final transaction = _createTransactionFromParsed(parsed, accounts, categories, msg.body ?? '');
//           await TransactionService().addTransaction(transaction);
//         }
//       }
//     } catch (e) {
//       // Handle errors silently or log
//       print('Error adding transactions from SMS: $e');
//     }
//   }

//   Transaction _createTransactionFromParsed(
//     ParsedTransaction parsed,
//     List<Account> accounts,
//     List<Category> categories,
//     String originalMessage,
//   ) {
//     String accountId = accounts.first.id; // Use first account
//     String categoryId = categories.first.id; // Use first category
//     if (parsed.categoryHint != null) {
//       final matchedCat = categories.firstWhere(
//         (c) => c.label.toLowerCase() == parsed.categoryHint!.toLowerCase(),
//         orElse: () => categories.first,
//       );
//       categoryId = matchedCat.id;
//     }

//     return Transaction(
//       transactionName: '${parsed.merchant ?? 'SMS Transaction'} Transaction',
//       amount: parsed.amount!,
//       type: 'Expense', // Assume expense for now
//       account: accountId,
//       category: categoryId,
//       occurredAt: parsed.date ?? DateTime.now(),
//       notes: 'Auto-added from SMS: $originalMessage',
//     );
//   }
// }
