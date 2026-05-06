import 'package:finance_app/features/auth/data/model/user_profile.dart';
import 'package:finance_app/features/auth/data/providers/auth_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final userProfileProvider = FutureProvider<UserProfile>((ref) {
  return ref.watch(authRepositoryProvider).fetchCurrentUser();
});
