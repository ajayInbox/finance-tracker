
import 'package:finance_app/data/models/category.dart';
import 'package:finance_app/data/repository/category_repository.dart';

class CategoryService {

  final CategoryRepository _repo = CategoryRepository();

  Future<List<Category>> getAllCategories() async {

    return await _repo.fetchAllCategories();
    
  }
}