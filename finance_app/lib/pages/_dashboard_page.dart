import 'package:finance_app/widgets/bar_chart.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:table_calendar/table_calendar.dart'; // [7]

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
          // TODO: navigate to Add Transaction
        },
        icon: const Icon(Icons.add),
        label: const Text('Add'),
      ),
      floatingActionButtonLocation: FloatingActionButtonLocation.endFloat,
    );
  }
}

class _DailySummaryCard extends StatelessWidget {
  const _DailySummaryCard({required this.currency});
  final String currency;

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
            Container(
              height: 150,
              decoration: BoxDecoration(
            //    color: const Color.fromARGB(255, 234, 233, 233),
                borderRadius: BorderRadius.circular(8),
              ),
              alignment: Alignment.center,
              child: ColumnDefault(),
            ),
            const SizedBox(height: 4),
            Container(
              color: const Color.fromARGB(255, 234, 233, 233), // same background for both rows
              padding: const EdgeInsets.all(12),
              child: Column( // inner Column contains the two rows
                children: [
                  Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('7 days average',
                    style: Theme.of(context).textTheme.bodyMedium),
                Text('$currency 0.00',
                    style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
            const SizedBox(height: 6),
            Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text('30 days average',
                    style: Theme.of(context).textTheme.bodyMedium),
                Text('-$currency 10.00',
                    style: Theme.of(context).textTheme.bodyMedium),
              ],
            ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

// class _BudgetSummaryCard extends StatelessWidget {
//   const _BudgetSummaryCard({required this.currency});
//   final String currency;

//   @override
//   Widget build(BuildContext context) {
//     return Card(
//       elevation: 1,
//       shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(12)),
//       child: Padding(
//         padding: const EdgeInsets.fromLTRB(16, 16, 16, 12),
//         child: Column(
//           crossAxisAlignment: CrossAxisAlignment.start,
//           children: [
//             _Header(title: 'Budget Summary', onSettings: () {}),
//             const SizedBox(height: 8),
//             AspectRatio(
//               aspectRatio: 16 / 9,
//               child: Center(
//                 child: Stack(
//                   alignment: Alignment.center,
//                   children: [
//                     SizedBox(
//                       width: 180,
//                       height: 180,
//                       child: CircularProgressIndicator(
//                         value: 0, // no data
//                         strokeWidth: 22,
//                         backgroundColor: Colors.red.shade300,
//                         valueColor:
//                             AlwaysStoppedAnimation<Color>(Colors.transparent),
//                       ),
//                     ),
//                     Text(
//                       'No Transactions',
//                       style: Theme.of(context).textTheme.titleMedium?.copyWith(
//                             color: Colors.white,
//                             backgroundColor: Colors.red.shade400,
//                           ),
//                     ),
//                   ],
//                 ),
//               ),
//             ),
//             const SizedBox(height: 12),
//             Row(
//               children: [
//                 Expanded(
//                   child: Text('Category',
//                       style: Theme.of(context).textTheme.titleMedium),
//                 ),
//                 Expanded(
//                   child: Text('Actual',
//                       style: Theme.of(context).textTheme.titleMedium),
//                 ),
//                 Expanded(
//                   child: Text('Budget',
//                       style: Theme.of(context).textTheme.titleMedium),
//                 ),
//               ],
//             ),
//             const SizedBox(height: 8),
//             Row(
//               children: [
//                 const Expanded(child: Text('Expense')),
//                 Expanded(child: Text('$currency 0.00')),
//                 Expanded(child: Text('$currency 0.00')),
//               ],
//             ),
//             const SizedBox(height: 8),
//             ClipRRect(
//               borderRadius: BorderRadius.circular(4),
//               child: LinearProgressIndicator(
//                 value: 0.0,
//                 minHeight: 8,
//                 backgroundColor: Colors.grey.shade200,
//                 color: Colors.red.shade400,
//               ),
//             ),
//             const SizedBox(height: 6),
//             Align(
//               alignment: Alignment.centerLeft,
//               child: Text(
//                 '${DateTime.now().day} ${_monthName(DateTime.now().month)}',
//                 style: Theme.of(context).textTheme.bodySmall,
//               ),
//             ),
//           ],
//         ),
//       ),
//     );
//   }

//   String _monthName(int m) {
//     const months = [
//       'January','February','March','April','May','June',
//       'July','August','September','October','November','December'
//     ];
//     return months[m - 1];
//   }
// }

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
