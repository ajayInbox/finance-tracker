import 'package:finance_app/data/models/category_breakdown.dart';

class ExpenseReport {

  final String month;
  final String currency;
  final double total;
  final List<CategoryBreakdown> categoryBreakdown;

  ExpenseReport({required this.month, required this.currency, required this.total, required this.categoryBreakdown});

  factory ExpenseReport.fromJson(Map<String, dynamic> json){
    return ExpenseReport(
      month: json['month'],
      currency: json['currency'],
      total: json['total'].toDouble(),
      categoryBreakdown: (json['byCategory'] as List)
      .map((item) => CategoryBreakdown.fromJson(item))
      .toList()
    );
  }
}
