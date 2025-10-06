import 'package:finance_app/widgets/bar_chart.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:finance_app/pages/add_transaction_page.dart';
import 'package:table_calendar/table_calendar.dart'; // [7]
import 'package:finance_app/data/services/transaction_service.dart';
import 'package:finance_app/data/models/transaction_summary.dart';

class DashboardPage extends StatefulWidget {
  const DashboardPage({super.key});

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends State<DashboardPage> {
  DateTime _focusedDay = DateTime.now(); // month in view [7]
  DateTime? _selectedDay;
  final currency = '₹';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      // appBar: AppBar(
      //   title: const Text('Main'),
      //   actions: [
      //     IconButton(onPressed: () {}, icon: const Icon(Icons.help_outline)),
      //   ],
      // ),
      body: ListView(
        padding: const EdgeInsets.all(5),
        children: [
          _DailySummaryCard(currency: currency),
          const SizedBox(height: 16),
          _CalendarCard(
            focusedDay: _focusedDay,
            selectedDay: _selectedDay,
            onDaySelected: (sel, foc) {
              setState(() {
                _selectedDay = sel;
                _focusedDay = foc;
              });
            },
          ),
          const SizedBox(height: 24),
        ],
      ),
      floatingActionButton: FloatingActionButton.extended(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const AddTransactionPage()),
          );
        },
        icon: const Icon(Icons.add),
        label: const Text('Add'),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.endFloat,
    );
  }
}

class _DailySummaryCard extends StatefulWidget {
  const _DailySummaryCard({this.currency = '₹'});
  final String currency;

  @override
  State<_DailySummaryCard> createState() => _DailySummaryCardState();
}

class _DailySummaryCardState extends State<_DailySummaryCard> {
  late Future<List<TransactionSummary>> _transactionsFuture;

  @override
  void initState() {
    super.initState();
    _transactionsFuture = TransactionService().getFeed();
  }

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 8, 16, 8),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            _Header(title: 'Daily Summary', onSettings: () {}),
            const SizedBox(height: 8),
            FutureBuilder<List<TransactionSummary>>(
              future: _transactionsFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting) {
                  return const SizedBox(
                    height: 150,
                    child: Center(child: CircularProgressIndicator()),
                  );
                }

                if (snapshot.hasError) {
                  return const SizedBox(
                    height: 150,
                    child: Center(child: Text('Failed to load chart data')),
                  );
                }

                final expenses = snapshot.data?.where((t) => t.type.toLowerCase() == 'expense').toList() ?? [];

                final dailyData = _calculateDailyExpenses(expenses);

                return SizedBox(
                  height: 150,
                  child: ColumnDefault(
                    data: dailyData.map((e) => ChartSampleData(x: e['x'], y: e['y'])).toList(),
                  ),
                );
              },
            ),
            const SizedBox(height: 4),
            FutureBuilder<List<TransactionSummary>>(
              future: _transactionsFuture,
              builder: (context, snapshot) {
                final expenses = snapshot.data?.where((t) => t.type.toLowerCase() == 'expense').toList() ?? [];

                final sevenDayAvg = _calculateAverage(expenses, const Duration(days: 7));
                final thirtyDayAvg = _calculateAverage(expenses, const Duration(days: 30));

                return Container(
                  color: Colors.grey.shade200,
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '7 days average',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                          Text(
                            '${widget.currency}${sevenDayAvg.toStringAsFixed(2)}',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                        ],
                      ),
                      const SizedBox(height: 6),
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '30 days average',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                          Text(
                            '${widget.currency}${thirtyDayAvg.toStringAsFixed(2)}',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                        ],
                      ),
                    ],
                  ),
                );
              },
            ),
          ],
        ),
      ),
    );
  }

  double _calculateAverage(List<TransactionSummary> expenses, Duration period) {
    final now = DateTime.now();
    final startDate = now.subtract(period);

    final filtered = expenses.where((t) => t.occuredAt.isAfter(startDate)).toList();

    if (filtered.isEmpty) return 0.0;

    return filtered.map((t) => t.amount).reduce((a, b) => a + b) / filtered.length;
  }

  List<Map<String, dynamic>> _calculateDailyExpenses(List<TransactionSummary> expenses) {
    final now = DateTime.now();
    final Map<String, double> dailySums = {};

    for (var expense in expenses) {
      if (expense.occuredAt.isAfter(now.subtract(const Duration(days: 7)))) {
        final day = expense.occuredAt.weekday; // 1 = Monday, 7 = Sunday
        final dayName = _getDayName(day);
        dailySums[dayName] = (dailySums[dayName] ?? 0) + expense.amount;
      }
    }

    final days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    return days.map((day) => {'x': day, 'y': dailySums[day] ?? 0.0}).toList();
  }

  String _getDayName(int weekday) {
    const days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
    return days[weekday - 1];
  }
}

class _CalendarCard extends StatelessWidget {
  const _CalendarCard({
    required this.focusedDay,
    required this.selectedDay,
    required this.onDaySelected,
  });

  final DateTime focusedDay;
  final DateTime? selectedDay;
  final void Function(DateTime selected, DateTime focused) onDaySelected;

  @override
  Widget build(BuildContext context) {
    return Card(
      elevation: 1,
      shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
      child: Padding(
        padding: const EdgeInsets.fromLTRB(16, 12, 16, 12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Calendar', style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 8),
            TableCalendar(
              firstDay: DateTime.utc(2015, 1, 1),
              lastDay: DateTime.utc(2035, 12, 31),
              focusedDay: focusedDay,
              calendarFormat: CalendarFormat.month,
              startingDayOfWeek: StartingDayOfWeek.sunday,
              selectedDayPredicate: (day) =>
                  selectedDay != null &&
                  day.year == selectedDay!.year &&
                  day.month == selectedDay!.month &&
                  day.day == selectedDay!.day,
              onDaySelected: onDaySelected,
              headerStyle: const HeaderStyle(
                formatButtonVisible: false,
                titleCentered: true,
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _Header extends StatelessWidget {
  const _Header({required this.title, required this.onSettings});
  final String title;
  final VoidCallback onSettings;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Expanded(
          child: Text(title, style: GoogleFonts.lato(
            textStyle: Theme.of(context).textTheme.titleMedium,
            fontWeight: FontWeight.w600,
          ),
          ),
        ),
        IconButton(
          onPressed: onSettings,
          icon: const Icon(Icons.settings),
          tooltip: 'Configure',
        ),
      ],
    );
  }
}
