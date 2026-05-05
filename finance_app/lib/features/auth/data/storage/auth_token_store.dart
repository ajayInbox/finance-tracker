import 'package:finance_app/features/auth/data/model/auth_tokens.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

final authTokenStoreProvider = Provider<AuthTokenStore>((ref) {
  return const AuthTokenStore();
});

class AuthTokenStore {
  const AuthTokenStore();

  static const accessTokenKey = 'access_token';
  static const refreshTokenKey = 'refresh_token';
  static const hasSeenLandingKey = 'has_seen_landing';

  Future<String?> readAccessToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(accessTokenKey);
  }

  Future<String?> readRefreshToken() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getString(refreshTokenKey);
  }

  Future<void> saveTokens(AuthTokens tokens) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setString(accessTokenKey, tokens.accessToken);
    await prefs.setString(refreshTokenKey, tokens.refreshToken);
  }

  Future<void> clearTokens() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.remove(accessTokenKey);
    await prefs.remove(refreshTokenKey);
  }

  Future<bool> hasSeenLanding() async {
    final prefs = await SharedPreferences.getInstance();
    return prefs.getBool(hasSeenLandingKey) ?? false;
  }

  Future<void> markLandingSeen() async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setBool(hasSeenLandingKey, true);
  }
}
