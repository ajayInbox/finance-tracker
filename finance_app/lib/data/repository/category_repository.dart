
import 'package:finance_app/data/models/category.dart';
import 'package:finance_app/utils/api_constants.dart';
import 'package:http/http.dart' as http;
import 'dart:convert';

class CategoryRepository {

  Future<List<Category>> fetchAllCategories() async {

    Uri uri = Uri.parse(ApiConstants.baseUrl).replace(
      path: ApiConstants.getCategories
    );

    final res = await http.get(
      uri,
      headers: {'Accept': 'application/json'},
    );

    if (res.statusCode != 200) {
      throw Exception('Failed to load transactions: ${res.statusCode}');
    }

    final body = jsonDecode(res.body);
    final List list = body is List ? body : (body['content'] as List);
    return list
        .cast<Map<String, dynamic>>()
        .map(Category.fromJson)
        .toList();
  }
}