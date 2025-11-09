import 'package:finance_app/data/models/account.dart';
import 'package:finance_app/data/repository/account_repository.dart';

class AccountService {

  final AccountRepository _repo = AccountRepository();

  Future<List<Account>> getAccounts() async {
    try {
      return await _repo.fetchAllAccounts();
    } catch (e) {
      // Return mock data if API fails
      throw Error();
    }
  }
}
