import 'package:dio/dio.dart';
import 'package:finance_app/features/auth/data/model/auth_tokens.dart';
import 'package:finance_app/utils/api_constants.dart';
import 'package:finance_app/utils/api_error_handler.dart';
import 'package:finance_app/utils/app_exception.dart';

class AuthRepository {
  const AuthRepository(this._dio);

  final Dio _dio;

  Future<AuthTokens> register({
    required String name,
    required String email,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiConstants.register,
        data: {'name': name, 'email': email, 'password': password},
      );
      return AuthTokens.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }

  Future<AuthTokens> login({
    required String email,
    required String password,
  }) async {
    try {
      final response = await _dio.post(
        ApiConstants.login,
        data: {'email': email, 'password': password},
      );
      return AuthTokens.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }

  Future<AuthTokens> refresh(String refreshToken) async {
    try {
      final response = await _dio.post(
        ApiConstants.refresh,
        data: {'refreshToken': refreshToken},
      );
      return AuthTokens.fromJson(response.data as Map<String, dynamic>);
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }

  Future<void> logout(String refreshToken) async {
    try {
      await _dio.post(
        ApiConstants.logout,
        data: {'refreshToken': refreshToken},
      );
    } catch (e) {
      throw AppException(ApiErrorHandler.getErrorMessage(e));
    }
  }
}
