class AverageDailyExpense {
  final String fromDate;
  final String toDate;
  final int days;
  final List<DailyExpense> dailyList;
  final double averageDailyExpense;

  AverageDailyExpense({
    required this.fromDate,
    required this.toDate,
    required this.days,
    required this.dailyList,
    required this.averageDailyExpense,
  });

  factory AverageDailyExpense.fromJson(Map<String, dynamic> json) {
    return AverageDailyExpense(
      fromDate: json['fromDate'],
      toDate: json['toDate'],
      days: json['days'],
      dailyList: (json['dailyList'] as List)
          .map((item) => DailyExpense.fromJson(item))
          .toList(),
      averageDailyExpense: json['averageDailyExpense'].toDouble(),
    );
  }
}

class DailyExpense {
  final String date;
  final double totalExpense;

  DailyExpense({
    required this.date,
    required this.totalExpense,
  });

  factory DailyExpense.fromJson(Map<String, dynamic> json) {
    return DailyExpense(
      date: json['date'],
      totalExpense: json['totalExpense'].toDouble(),
    );
  }
}
