import 'package:finance_app/features/category/data/models/category.dart';
import 'package:finance_app/features/category/data/providers/category_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final categoriesProvider = FutureProvider<List<Category>>((ref) async {
  final repo = ref.watch(categoryRepositoryProvider);
  return repo.getAllCategories();
});
