import 'package:dio/dio.dart';
import 'package:finance_app/features/category/data/models/category.dart';
import 'package:finance_app/utils/api_constants.dart';

class CategoryRepository {
  final Dio dio;

  CategoryRepository(this.dio);

  Future<List<Category>> getAllCategories() async {
    final res = await dio.get(ApiConstants.getCategories);

    final data = res.data;
    final List list = data is List ? data : (data['content'] as List);

    return list.cast<Map<String, dynamic>>().map(Category.fromJson).toList();
  }

  Future<Category> createCategory(Map<String, dynamic> data) async {
    print(data);
    final res = await dio.post(ApiConstants.createCategory, data: data);
    return Category.fromJson(res.data as Map<String, dynamic>);
  }

  Future<Category> updateCategory(String id, Map<String, dynamic> data) async {
    final res = await dio.put('${ApiConstants.getCategories}/$id', data: data);
    return Category.fromJson(res.data as Map<String, dynamic>);
  }

  Future<void> deleteCategory(String id) async {
    await dio.delete('${ApiConstants.getCategories}/$id');
  }
}
