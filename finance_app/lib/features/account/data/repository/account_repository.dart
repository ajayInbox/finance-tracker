import 'package:dio/dio.dart';
import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/account/data/model/account_create_update_request.dart';
import 'package:finance_app/features/account/data/model/networth_summary.dart';
import 'package:finance_app/utils/api_constants.dart';

class AccountRepository {
  final Dio dio;

  AccountRepository(this.dio);

  Future<List<Account>> fetchAllAccounts() async {
    final res = await dio.get(ApiConstants.getAccounts);

    final data = res.data;
    final List list = data is List ? data : (data['content'] as List);

    return list
        .cast<Map<String, dynamic>>()
        .map(Account.fromJson)
        .toList();
  }

  Future<Account> createAccount(AccountCreateUpdateRequest request) async {
    final res = await dio.post(
      ApiConstants.createAccount,
      data: request.toJson(),
    );

    return Account.fromJson(res.data);
  }

  Future<NetworthSummary> getNetWorth() async {
    final res = await dio.get(
      ApiConstants.networthSummary,
      queryParameters: {'userId': 'ABC'},
    );

    return NetworthSummary.fromJson(res.data);
  }
}
