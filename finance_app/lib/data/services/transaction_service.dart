import '../repository/transaction_repository.dart';
import '../models/_transaction.dart';

// application/service
class TransactionService {
  TransactionService(this._repo);
  final TransactionRepository _repo;

  // Example orchestration: fetch and sort descending by date
  Future<List<Transaction>> getFeed() async {
    final items = await _repo.fetchAll();
    return items;
  }

  // More business rules can live here: filtering, currency conversion, etc.
}
