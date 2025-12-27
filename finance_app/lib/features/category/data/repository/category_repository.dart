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

    return list
        .cast<Map<String, dynamic>>()
        .map(Category.fromJson)
        .toList();
  }

}