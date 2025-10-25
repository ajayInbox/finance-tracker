import 'package:finance_app/widgets/bar_chart.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:table_calendar/table_calendar.dart';
import 'package:finance_app/data/services/transaction_service.dart';
import 'package:finance_app/data/models/average_daily_expense.dart';
import 'package:intl/intl.dart';

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
  late Future<AverageDailyExpense> _averageDailyExpenseFuture;

  @override
  void initState() {
    super.initState();
    _averageDailyExpenseFuture = TransactionService().getAverageDailyExpense();
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
            FutureBuilder<AverageDailyExpense>(
              future: _averageDailyExpenseFuture,
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

                final dailyData = snapshot.data!.dailyList;

                return SizedBox(
                  height: 150,
                  child: ColumnDefault(
                    data: dailyData.map((e) => ChartSampleData(
                      x: _formatDate(e.date),
                      y: e.totalExpense
                    )).toList(),
                  ),
                );
              },
            ),
            const SizedBox(height: 4),
            FutureBuilder<AverageDailyExpense>(
              future: _averageDailyExpenseFuture,
              builder: (context, snapshot) {
                if (snapshot.connectionState == ConnectionState.waiting ||
                    snapshot.hasError ||
                    !snapshot.hasData) {
                  return const SizedBox();
                }

                final data = snapshot.data!;

                return Container(
                  color: Colors.grey.shade200,
                  padding: const EdgeInsets.all(12),
                  child: Column(
                    children: [
                      Row(
                        mainAxisAlignment: MainAxisAlignment.spaceBetween,
                        children: [
                          Text(
                            '${data.days} days average',
                            style: Theme.of(context).textTheme.bodyMedium,
                          ),
                          Text(
                            '${widget.currency}${data.averageDailyExpense.toStringAsFixed(2)}',
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

  String _formatDate(String date) {
    final parsedDate = DateTime.parse(date);
    return DateFormat('MMM\nd').format(parsedDate); // e.g., "Oct 7"
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
