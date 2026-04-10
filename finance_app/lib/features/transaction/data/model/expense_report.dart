import 'package:finance_app/features/transaction/data/model/category_breakdown.dart';

class ExpenseReport {

  final DateTime startDate;
  final DateTime endDate;
  final String currency;
  final double total;
  final List<CategoryBreakdown> categoryBreakdown;

  ExpenseReport({required this.startDate, required this.endDate, required this.currency, required this.total, required this.categoryBreakdown});

  factory ExpenseReport.fromJson(Map<String, dynamic> json){
    return ExpenseReport(
      startDate: DateTime.parse(json['startDate']),
      endDate: DateTime.parse(json['endDate']),
      currency: json['currency'],
      total: json['total'].toDouble(),
      categoryBreakdown: (json['byCategory'] as List)
      .map((item) => CategoryBreakdown.fromJson(item))
      .toList()
    );
  }
}
