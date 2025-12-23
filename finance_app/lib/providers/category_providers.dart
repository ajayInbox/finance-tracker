import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:finance_app/data/models/category.dart';
import 'package:finance_app/data/services/category_service.dart';

/// Service provider
final categoryServiceProvider = Provider<CategoryService>((ref) {
  return CategoryService();
});

/// Categories list provider
final categoriesProvider = FutureProvider<List<Category>>((ref) async {
  final service = ref.watch(categoryServiceProvider);
  return service.getAllCategories();
});
