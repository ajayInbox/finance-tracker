import 'package:finance_app/features/category/data/models/category.dart';
import 'package:finance_app/features/category/data/providers/category_repository_provider.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final categoryControllerProvider =
    AsyncNotifierProvider<CategoryController, List<Category>>(
      CategoryController.new,
    );

class CategoryController extends AsyncNotifier<List<Category>> {
  @override
  Future<List<Category>> build() async {
    final repo = ref.watch(categoryRepositoryProvider);
    return repo.getAllCategories();
  }

  Future<void> refresh() async {
    state = const AsyncLoading();
    state = await AsyncValue.guard(() async {
      final repo = ref.read(categoryRepositoryProvider);
      return repo.getAllCategories();
    });
  }

  Future<void> createCategory(Map<String, dynamic> data) async {
    await ref.read(categoryRepositoryProvider).createCategory(data);
    await refresh();
  }
}
