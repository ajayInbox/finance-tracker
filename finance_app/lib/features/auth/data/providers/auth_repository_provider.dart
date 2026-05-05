import 'package:finance_app/core/dio_provider.dart';
import 'package:finance_app/features/auth/data/repository/auth_repository.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(ref.watch(dioProvider));
});
