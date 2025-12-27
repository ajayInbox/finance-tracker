import 'package:finance_app/features/category/data/repository/category_repository.dart';
import 'package:finance_app/core/dio_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final categoryRepositoryProvider = Provider<CategoryRepository>((ref) {
  final dio = ref.watch(dioProvider);
  return CategoryRepository(dio);
});