import 'package:finance_app/data/models/account.dart';
import 'package:finance_app/data/models/account_create_update_request.dart';
import 'package:finance_app/data/models/networth_summary.dart';
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

  Future<Account> createAccount(AccountCreateUpdateRequest request) async {
    try {
      return await _repo.createAccount(request);
    } catch (e) {
      throw Exception('Failed to create account: $e');
    }
  }

  Future<NetworthSummary> getNetWorth() async {
    try {
      return await _repo.getNetWorth();
    } catch (e) {
      // Return mock data if API fails
      throw Error();
    }
  }
}
