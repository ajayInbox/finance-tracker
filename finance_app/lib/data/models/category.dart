class Category {

  final String id;
  final String key;
  final String label;

  Category({required this.id, required this.key, required this.label});

  factory Category.fromJson(Map<String,dynamic> j) => Category(
    id: j['id'] as String,
    key: j['key'] as String,
    label: j['label'] as String
  );
  
}