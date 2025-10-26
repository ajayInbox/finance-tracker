class Category {

  final String id;
  final String key;
  final String label;
  final bool active;
  final bool isExpense;
  final bool isIncome;

  Category({required this.id, 
    required this.key, 
    required this.label,
    required this.active,
    required this.isExpense,
    required this.isIncome
  });

  factory Category.fromJson(Map<String,dynamic> j) => Category(
    id: j['id'] as String,
    key: j['key'] as String,
    label: j['label'] as String,
    active: j['active'] as bool,
    isExpense: j['isExpense'] as bool,
    isIncome: j['isIncome'] as bool,
  );
  
}