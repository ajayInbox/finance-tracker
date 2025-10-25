class CategoryBreakdown {

  final String categoryId;
  final String categoryName;
  final double total;
  final double percentage;
  final int transactionCount;

  CategoryBreakdown({required this.categoryId,
  required this.categoryName,
  required this.total,
  required this.percentage,
  required this.transactionCount});

  factory CategoryBreakdown.fromJson(Map<String, dynamic> json){
    return CategoryBreakdown(
      categoryId: json['categoryId'],
      categoryName: json['categoryName'],
      total: json['total'].toDouble(),
      percentage: json['percentage'].toDouble(),
      transactionCount: json['transactionCount']
    );
  }

}
