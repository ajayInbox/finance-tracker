class Category {
  final String id;
  final String name;
  final String? description;
  final String type;
  final bool isActive;
  final String? parentId;
  final List<Category> children;
  final String iconKey;
  final String colorCode;

  Category({
    required this.id,
    required this.name,
    this.description,
    required this.type,
    required this.isActive,
    this.parentId,
    this.children = const [],
    required this.iconKey,
    required this.colorCode,
  });

  factory Category.fromJson(Map<String, dynamic> j) {
    List<Category> parsedChildren = [];
    if (j['children'] != null && j['children'] is List) {
      parsedChildren = (j['children'] as List)
          .map(
            (childProps) =>
                Category.fromJson(childProps as Map<String, dynamic>),
          )
          .toList();
    }

    return Category(
      id: j['id']?.toString() ?? '',
      name: j['name']?.toString() ?? '',
      description: j['description']?.toString(),
      type: j['type']?.toString() ?? 'EXPENSE',
      isActive: (j['isActive'] ?? j['active'] ?? true) == true,
      parentId: j['parentId']?.toString(),
      children: parsedChildren,
      iconKey: j['iconKey'].toString(),
      colorCode: j['colorCode'].toString(),
    );
  }

  bool isExpense() => type == 'EXPENSE';
  bool isIncome() => type == 'INCOME';
}
