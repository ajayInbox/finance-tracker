import '../repository/transaction_repository.dart';
import '../models/transaction_summary.dart';
import '../models/transaction.dart';

// application/service
class TransactionService {
  final TransactionRepository _repo = TransactionRepository();

  // Example orchestration: fetch and sort descending by date
  Future<List<TransactionSummary>> getFeed() async {
    final items = await _repo.fetchAllTransactions();
    return items;
  }

  Future<void> addTransaction(Transaction transaction) async {
    await _repo.createTransaction(transaction);
  }

  // More business rules can live here: filtering, currency conversion, etc.
}
