import 'package:dio/dio.dart';
import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/account/data/model/account_create_update_request.dart';
import 'package:finance_app/features/account/data/model/networth_summary.dart';
import 'package:finance_app/utils/api_constants.dart';
import 'package:finance_app/utils/api_error_handler.dart';
import 'package:finance_app/utils/app_exception.dart';

class AccountRepository {
  final Dio dio;

  AccountRepository(this.dio);

  Future<List<Account>> fetchAllAccounts() async {
    try {
      final res = await dio.get(ApiConstants.getAccounts);
      final data = res.data;
      final List list = data is List ? data : (data['content'] as List);
      return list.cast<Map<String, dynamic>>().map(Account.fromJson).toList();
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }

  Future<Account> createAccount(AccountCreateUpdateRequest request) async {
    try {
      final res = await dio.post(
        ApiConstants.createAccount,
        data: request.toJson(),
      );
      return Account.fromJson(res.data);
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }

  Future<NetworthSummary> getNetWorth() async {
    try {
      final res = await dio.get(
        ApiConstants.networthSummary,
        queryParameters: {'userId': 'ABC'},
      );
      return NetworthSummary.fromJson(res.data);
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }

  Future<void> deleteAccount(String id) async {
    try {
      await dio.delete('${ApiConstants.deleteAccount}/$id');
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }

  Future<Account> updateAccount(
    String id,
    AccountCreateUpdateRequest request,
  ) async {
    try {
      print(request.toJson());
      final res = await dio.put(
        '${ApiConstants.updateAccount}/$id',
        data: request.toJson(),
      );

      return Account.fromJson(res.data);
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }
}
