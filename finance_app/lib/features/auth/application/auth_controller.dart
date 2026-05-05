import 'package:finance_app/features/auth/data/providers/auth_repository_provider.dart';
import 'package:finance_app/features/auth/data/storage/auth_token_store.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final authControllerProvider = AsyncNotifierProvider<AuthController, AuthState>(
  AuthController.new,
);

class AuthState {
  const AuthState({
    required this.isAuthenticated,
    required this.hasSeenLanding,
  });

  final bool isAuthenticated;
  final bool hasSeenLanding;

  AuthState copyWith({bool? isAuthenticated, bool? hasSeenLanding}) {
    return AuthState(
      isAuthenticated: isAuthenticated ?? this.isAuthenticated,
      hasSeenLanding: hasSeenLanding ?? this.hasSeenLanding,
    );
  }
}

class AuthController extends AsyncNotifier<AuthState> {
  @override
  Future<AuthState> build() async {
    final store = ref.read(authTokenStoreProvider);
    final accessToken = await store.readAccessToken();
    final hasSeenLanding = await store.hasSeenLanding();

    return AuthState(
      isAuthenticated: accessToken != null && accessToken.isNotEmpty,
      hasSeenLanding: hasSeenLanding,
    );
  }

  Future<void> markLandingSeen() async {
    final store = ref.read(authTokenStoreProvider);
    await store.markLandingSeen();
    final previous = state.asData?.value;
    state = AsyncData(
      (previous ??
              const AuthState(isAuthenticated: false, hasSeenLanding: true))
          .copyWith(hasSeenLanding: true),
    );
  }

  Future<void> login({required String email, required String password}) async {
    final repo = ref.read(authRepositoryProvider);
    final store = ref.read(authTokenStoreProvider);
    final tokens = await repo.login(email: email, password: password);
    await store.saveTokens(tokens);
    await store.markLandingSeen();
    state = const AsyncData(
      AuthState(isAuthenticated: true, hasSeenLanding: true),
    );
  }

  Future<void> register({
    required String name,
    required String email,
    required String password,
  }) async {
    final repo = ref.read(authRepositoryProvider);
    final store = ref.read(authTokenStoreProvider);
    final tokens = await repo.register(
      name: name,
      email: email,
      password: password,
    );
    await store.saveTokens(tokens);
    await store.markLandingSeen();
    state = const AsyncData(
      AuthState(isAuthenticated: true, hasSeenLanding: true),
    );
  }

  Future<void> logout() async {
    final repo = ref.read(authRepositoryProvider);
    final store = ref.read(authTokenStoreProvider);
    final refreshToken = await store.readRefreshToken();

    try {
      if (refreshToken != null && refreshToken.isNotEmpty) {
        await repo.logout(refreshToken);
      }
    } finally {
      await store.clearTokens();
      state = const AsyncData(
        AuthState(isAuthenticated: false, hasSeenLanding: true),
      );
    }
  }

  Future<void> clearSession() async {
    final store = ref.read(authTokenStoreProvider);
    await store.clearTokens();
    state = const AsyncData(
      AuthState(isAuthenticated: false, hasSeenLanding: true),
    );
  }
}
