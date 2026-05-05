import 'dart:async';

import 'package:dio/dio.dart';
import 'package:finance_app/features/auth/application/auth_controller.dart';
import 'package:finance_app/features/auth/data/model/auth_tokens.dart';
import 'package:finance_app/features/auth/data/storage/auth_token_store.dart';
import 'package:finance_app/utils/api_constants.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

/// Mutex for token refresh — prevents multiple concurrent 401 responses
/// from each triggering independent refresh calls that race and invalidate
/// each other's tokens.
Completer<AuthTokens>? _refreshCompleter;

final dioProvider = Provider<Dio>((ref) {
  final tokenStore = ref.watch(authTokenStoreProvider);
  final dio = Dio(
    BaseOptions(
      baseUrl: ApiConstants.baseUrl,
      connectTimeout: const Duration(seconds: 5),
      receiveTimeout: const Duration(seconds: 5),
      headers: {'Accept': 'application/json'},
    ),
  );

  dio.interceptors.add(
    InterceptorsWrapper(
      onRequest: (options, handler) async {
        if (!_isAuthEndpoint(options.path)) {
          final token = await tokenStore.readAccessToken();
          if (token != null && token.isNotEmpty) {
            options.headers['Authorization'] = 'Bearer $token';
          }
        }
        handler.next(options);
      },
      onError: (error, handler) async {
        final request = error.requestOptions;
        final alreadyRetried = request.extra['authRetry'] == true;

        if (error.response?.statusCode != 401 ||
            alreadyRetried ||
            _isAuthEndpoint(request.path)) {
          handler.next(error);
          return;
        }

        try {
          final AuthTokens tokens;

          if (_refreshCompleter != null) {
            // Another refresh is already in-flight — wait for it
            tokens = await _refreshCompleter!.future;
          } else {
            // First 401 — take the lock and perform the refresh
            _refreshCompleter = Completer<AuthTokens>();
            try {
              tokens = await _performTokenRefresh(tokenStore, ref);
              _refreshCompleter!.complete(tokens);
            } catch (e) {
              _refreshCompleter!.completeError(e);
              rethrow;
            } finally {
              _refreshCompleter = null;
            }
          }

          // Retry the original request with the new access token
          final retryOptions = request.copyWith(
            headers: {
              ...request.headers,
              'Authorization': 'Bearer ${tokens.accessToken}',
            },
            extra: {...request.extra, 'authRetry': true},
          );
          final retryResponse = await dio.fetch<dynamic>(retryOptions);
          handler.resolve(retryResponse);
        } catch (_) {
          await _clearExpiredSession(ref);
          handler.next(error);
        }
      },
    ),
  );

  return dio;
});

/// Performs the actual token refresh and saves the new tokens.
/// Called only once per refresh cycle (guarded by [_refreshCompleter]).
Future<AuthTokens> _performTokenRefresh(
  AuthTokenStore tokenStore,
  Ref ref,
) async {
  final refreshToken = await tokenStore.readRefreshToken();
  if (refreshToken == null || refreshToken.isEmpty) {
    throw Exception('No refresh token available');
  }

  final refreshDio = Dio(
    BaseOptions(
      baseUrl: ApiConstants.baseUrl,
      connectTimeout: const Duration(seconds: 5),
      receiveTimeout: const Duration(seconds: 5),
      headers: {'Accept': 'application/json'},
    ),
  );
  final refreshResponse = await refreshDio.post(
    ApiConstants.refresh,
    data: {'refreshToken': refreshToken},
  );
  final tokens = AuthTokens.fromJson(
    refreshResponse.data as Map<String, dynamic>,
  );
  await tokenStore.saveTokens(tokens);
  return tokens;
}

bool _isAuthEndpoint(String path) {
  return path == ApiConstants.register ||
      path == ApiConstants.login ||
      path == ApiConstants.refresh ||
      path == ApiConstants.logout;
}

Future<void> _clearExpiredSession(Ref ref) async {
  await ref.read(authControllerProvider.notifier).clearSession();
}
