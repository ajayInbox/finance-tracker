// lib/pages/dashboard_page.dart
import 'package:finance_app/features/transaction/application/transaction_controller.dart';
import 'package:finance_app/features/transaction/data/model/expense_report.dart';
import 'package:finance_app/features/transaction/data/model/category_breakdown.dart';
import 'package:finance_app/features/account/data/model/networth_summary.dart';
import 'package:finance_app/features/transaction/data/model/transaction_result.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';
import 'package:finance_app/features/account/provider/networth_provider.dart';
import 'package:finance_app/features/transaction/providers/expense_report_provider.dart';
import 'package:finance_app/features/transaction/ui/transaction_form_page.dart';

import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

import 'package:finance_app/features/transaction/ui/transactions_page.dart';
import 'package:finance_app/features/transaction/ui/widgets/transaction_card.dart';
import 'dart:math' as math;
import 'package:finance_app/widgets/app_page_header.dart';

class DashboardPage extends ConsumerStatefulWidget {
  const DashboardPage({super.key});

  @override
  ConsumerState<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends ConsumerState<DashboardPage>
    with TickerProviderStateMixin {
  final String currency = '₹';

  // Animation controllers
  late AnimationController _fadeController;
  late AnimationController _slideController;

  @override
  void initState() {
    super.initState();

    // Initialize animations
    _fadeController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    )..forward();

    _slideController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    )..forward();
  }

  @override
  void dispose() {
    _fadeController.dispose();
    _slideController.dispose();
    super.dispose();
  }

  Future<void> _handleRefresh() async {
    return Future.wait([
      ref.read(transactionsControllerProvider.notifier).refresh(),
      ref.refresh(expenseReportProvider.future),
      ref.refresh(networthProvider.future),
    ]).then((_) {});
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      floatingActionButton: _buildFloatingActionButton(),
      body: Stack(
        children: [
          RefreshIndicator(
            onRefresh: _handleRefresh,
            child: SingleChildScrollView(
              physics: const AlwaysScrollableScrollPhysics(),
              padding: EdgeInsets.fromLTRB(
                20,
                0,
                20,
                100,
              ), // Added bottom padding for FAB
              child: FadeTransition(
                opacity: _fadeController,
                child: SlideTransition(
                  position: Tween<Offset>(
                    begin: const Offset(0, 0.1),
                    end: Offset.zero,
                  ).animate(_slideController),
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      AppPageHeader(
                        padding: EdgeInsets.only(top: 48, bottom: 20),
                        leading: Row(
                          children: [
                            Container(
                              decoration: BoxDecoration(
                                shape: BoxShape.circle,
                                border: Border.all(color: Theme.of(context).colorScheme.primary, width: 2),
                              ),
                              padding: EdgeInsets.all(2),
                              child: ClipRRect(
                                borderRadius: BorderRadius.circular(50),
                                child: Image.network(
                                  'https://lh3.googleusercontent.com/aida-public/AB6AXuCuWLr3iXcmohbUN1dzVItsdGjg8eWe7ldYfbpv56Jwox266gr4PLp2dx8QfjcKw4h87Zx8jW-6uuR1P7IJbtSUl_2qYiuV-ieL_vCF5nbKgZN0RX5X1Mzvlp7Kt6PgMKbBZZHVnW1sHTiYTMKDqKqR91ALFoJHB0_lQqOaNokVh1O4-5AYAJ5ZUNJrtJ5E6ppyZgGymaNi3NybEm6Ml7JUAHn2IKJmIf-UCf7E73MWXoMKcc60BZbIxeM23pef2QjoeRuqotWNiim1',
                                  width: 48,
                                  height: 48,
                                  fit: BoxFit.cover,
                                  errorBuilder: (context, error, stackTrace) => Container(
                                    width: 48,
                                    height: 48,
                                    color: Theme.of(context).dividerColor,
                                    child: Icon(Icons.person, color: Colors.grey),
                                  ),
                                ),
                              ),
                            ),
                            SizedBox(width: 16),
                            Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'WELCOME BACK',
                                  style: GoogleFonts.plusJakartaSans(
                                    fontSize: 12,
                                    fontWeight: FontWeight.w600,
                                    color: Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey,
                                    letterSpacing: 1.0,
                                  ),
                                ),
                                Text(
                                  'Alex Johnson',
                                  style: GoogleFonts.plusJakartaSans(
                                    fontSize: 20,
                                    fontWeight: FontWeight.bold,
                                    color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                                  ),
                                ),
                              ],
                            ),
                          ],
                        ),
                      ),
                      SizedBox(height: 24),
                      _buildTopSummaryCards(),
                      SizedBox(height: 24),
                      _buildSpendingAnalysisCard(),
                      SizedBox(height: 24),
                      _buildRecentTransactions(), // Preserved
                      SizedBox(height: 24),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }



  Widget _buildFloatingActionButton() {
    return Padding(
      padding: EdgeInsets.only(bottom: 80),
      child: SizedBox(
        width: 56,
        height: 56,
        child: FloatingActionButton(
          backgroundColor: Theme.of(context).colorScheme.primary, // primary
          elevation: 8, // shadow-glow approx
          shape: CircleBorder(),
          onPressed: () => _openTransactionForm(null),
          child: Icon(Icons.add, color: Theme.of(context).colorScheme.surface, size: 28),
        ),
      ),
    );
  }

  Future<void> _openTransactionForm(TransactionSummary? transaction) async {
    final result = await Navigator.push<TransactionResult>(
      context,
      MaterialPageRoute(
        builder: (_) => TransactionFormPage(transaction: transaction),
      ),
    );

    if (!mounted || result == null) return;

    if (result == TransactionResult.success) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            transaction == null
                ? 'Transaction added successfully'
                : 'Transaction updated successfully',
          ),
          backgroundColor: Colors.green,
        ),
      );
    } else {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            transaction == null
                ? 'Failed to add transaction'
                : 'Failed to update transaction',
          ),
          backgroundColor: Theme.of(context).colorScheme.error,
        ),
      );
    }
  }

  // Enhanced Top Summary Cards with Sparklines
  Widget _buildTopSummaryCards() {
    final expenseAsync = ref.watch(expenseReportProvider);
    final networthAsync = ref.watch(networthProvider);
    return expenseAsync.when(
      loading: () => _buildTopLoading(),
      error: (_, __) => _buildTopError(),
      data: (report) {
        return networthAsync.when(
          loading: () => _buildTopLoading(),
          error: (_, __) => _buildTopError(),
          data: (networth) {
            return _buildTopCards(networth, report);
          },
        );
      },
    );
  }

  Widget _buildTopLoading() => Row(
    children: [
      Expanded(child: _buildLoadingSummaryCard()),
      SizedBox(width: 16),
      Expanded(child: _buildLoadingSummaryCard()),
    ],
  );

  Widget _buildTopError() => Row(
    children: [
      Expanded(child: _buildErrorSummaryCard()),
      SizedBox(width: 16),
      Expanded(child: _buildErrorSummaryCard()),
    ],
  );

  Widget _buildLoadingSummaryCard() {
    return Container(
      height: 180,
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 80, height: 12, color: Theme.of(context).dividerColor),
          SizedBox(height: 12),
          Container(width: 100, height: 24, color: Theme.of(context).dividerColor),
        ],
      ),
    );
  }

  Widget _buildErrorSummaryCard() {
    return Container(
      height: 180,
      padding: EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.error.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(24),
      ),
      child: Center(child: Text('Error loading data')),
    );
  }

  Widget _buildTopCards(NetworthSummary networth, ExpenseReport report) {
    return Row(
      children: [
        Expanded(
          child: _buildGlowCard(
            title: 'Expenses',
            amount: '₹ ${report.total.toStringAsFixed(0)}',
            trend: '+5%',
            trendUp: true,
            icon: Icons.credit_card,
            color: Theme.of(context).colorScheme.error, // Red 500
            bgColor: Theme.of(context).colorScheme.error,
            glowColor: Theme.of(context).colorScheme.error,
          ),
        ),
        SizedBox(width: 16),
        Expanded(
          child: _buildGlowCard(
            title: 'Total Balance',
            amount: networth.formattedNetWorth,
            trend: '+12%',
            trendUp: true,
            icon: Icons.account_balance_wallet,
            color: Theme.of(context).colorScheme.primary,
            bgColor: Theme.of(context).colorScheme.primary,
            glowColor: Theme.of(context).colorScheme.primary,
          ),
        ),
      ],
    );
  }

  Widget _buildGlowCard({
    required String title,
    required String amount,
    required String trend,
    required bool trendUp,
    required IconData icon,
    required Color color,
    required Color bgColor,
    required Color glowColor,
  }) {
    return Container(
      height: 180,
      clipBehavior: Clip.hardEdge,
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(32),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04), // soft shadow
            blurRadius: 40,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Stack(
        children: [
          Positioned(
            top: -40,
            right: -40,
            child: Container(
              width: 128, // Reverted to MD approx (w-32 = 128px)
              height: 128,
              decoration: BoxDecoration(
                color: glowColor.withValues(
                  alpha: 0.1,
                ), // blur-2xl equivalent approx
                shape: BoxShape.circle,
                boxShadow: [
                  BoxShadow(
                    color: glowColor.withValues(alpha: 0.2),
                    blurRadius: 50,
                    spreadRadius: 20,
                  ),
                ],
              ),
            ),
          ),
          Padding(
            padding: EdgeInsets.all(20),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Container(
                      width: 40, // w-10
                      height: 40, // h-10
                      decoration: BoxDecoration(
                        color: bgColor.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(12), // rounded-xl
                      ),
                      child: Icon(
                        icon,
                        color: color,
                        size: 24,
                      ), // text-xl material icon approx 24
                    ),
                    SizedBox(width: 12), // gap-3 = 12px
                    Expanded(
                      child: Text(
                        title,
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14, // text-sm
                          fontWeight: FontWeight.w500, // font-medium
                          color: Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey, // text-secondary
                        ),
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                  ],
                ),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    FittedBox(
                      fit: BoxFit.scaleDown,
                      alignment: Alignment.centerLeft,
                      child: Text(
                        amount,
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 24, // text-2xl
                          fontWeight: FontWeight.w700, // font-bold
                          color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                          letterSpacing: -0.5, // tracking-tight
                        ),
                      ),
                    ),
                    SizedBox(
                      height: 12,
                    ), // mb-3 = 12px margin bottom on h2
                    Container(
                      padding: EdgeInsets.symmetric(
                        horizontal: 12, // px-3
                        vertical: 4, // py-1
                      ),
                      decoration: BoxDecoration(
                        color: trendUp
                            ? Theme.of(context).colorScheme.primary.withValues(alpha: 0.1)
                            : Theme.of(context).colorScheme.error.withValues(alpha: 0.1), // green-50 : red-50
                        borderRadius: BorderRadius.circular(
                          999,
                        ), // rounded-full
                        border: Border.all(
                          color: trendUp
                              ? const Color(0xFFD1FAE5)
                              : const Color(0xFFFEE2E2), // green-100 : red-100
                        ),
                      ),
                      child: Row(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Icon(
                            Icons.trending_up,
                            size:
                                16, // text-xs is small, but icon needs to be visible. Material default small is often 16-18. text-xs is 12px.
                            color: trendUp
                                ? Theme.of(context).colorScheme.primary
                                : const Color(
                                    0xFFDC2626,
                                  ), // green-600 : red-600
                          ),
                          SizedBox(width: 4), // gap-1
                          Text(
                            trend,
                            style: GoogleFonts.plusJakartaSans(
                              fontSize: 12, // text-xs
                              fontWeight: FontWeight.w700, // font-bold
                              color: trendUp
                                  ? Theme.of(context).colorScheme.primary
                                  : const Color(
                                      0xFFB91C1C,
                                    ), // green-700 : red-700
                            ),
                          ),
                        ],
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSparklineChart(List<double> data, Color color) {
    return LineChart(
      LineChartData(
        gridData: FlGridData(show: false),
        titlesData: FlTitlesData(show: false),
        borderData: FlBorderData(show: false),
        minX: 0,
        maxX: (data.length - 1).toDouble(),
        minY: data.reduce(math.min) * 0.8,
        maxY: data.reduce(math.max) * 1.2,
        lineBarsData: [
          LineChartBarData(
            spots: data.asMap().entries.map((e) {
              return FlSpot(e.key.toDouble(), e.value);
            }).toList(),
            isCurved: true,
            color: color,
            barWidth: 2,
            isStrokeCapRound: true,
            dotData: FlDotData(show: false),
            belowBarData: BarAreaData(show: false),
          ),
        ],
      ),
    );
  }

  Widget _buildSpendingAnalysisCard() {
    final expenseAsync = ref.watch(expenseReportProvider);
    final selectedPeriod = ref.watch(expenseReportPeriodProvider);
    return Container(
      padding: EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(40), // rounded-[2.5rem]
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 40,
            offset: const Offset(0, 10),
          ),
        ],
        border: Border.all(color: Colors.transparent),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Spending Analysis',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 20,
                      fontWeight: FontWeight.w700,
                      color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                    ),
                  ),
                  SizedBox(height: 4),
                  Text(
                    'Monthly Breakdown',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 14,
                      color: Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey,
                    ),
                  ),
                ],
              ),
              IconButton(
                icon: Icon(Icons.more_horiz, color: Colors.grey),
                onPressed: () {},
              ),
            ],
          ),
          SizedBox(height: 24),
          // Time period selector
          Container(
            padding: EdgeInsets.all(4),
            decoration: BoxDecoration(
              color: Theme.of(context).scaffoldBackgroundColor, // gray-100
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              children: ['1W', '1M', '3M', '6M', '1Y'].map((period) {
                final isSelected = selectedPeriod == period;
                return Expanded(
                  child: GestureDetector(
                    onTap: () {
                      ref
                          .read(expenseReportPeriodProvider.notifier)
                          .setPeriod(period);
                    },
                    child: Container(
                      padding: EdgeInsets.symmetric(vertical: 8),
                      decoration: isSelected
                          ? BoxDecoration(
                              color: Theme.of(context).colorScheme.surface,
                              borderRadius: BorderRadius.circular(12),
                              boxShadow: [
                                BoxShadow(
                                  color: Colors.black.withValues(alpha: 0.05),
                                  blurRadius: 4,
                                  offset: const Offset(0, 1),
                                ),
                              ],
                            )
                          : BoxDecoration(),
                      child: Center(
                        child: Text(
                          period,
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 14,
                            fontWeight: isSelected
                                ? FontWeight.w700
                                : FontWeight.w500,
                            color: isSelected
                                ? Theme.of(context).textTheme.titleLarge?.color
                                : Theme.of(context).textTheme.bodySmall?.color,
                          ),
                        ),
                      ),
                    ),
                  ),
                );
              }).toList(),
            ),
          ),
          SizedBox(height: 40),
          // Pie chart
          Center(
            child: SizedBox(
              height: 250,
              width: 250,
              child: expenseAsync.when(
                loading: () => const CircularProgressIndicator(),
                error: (_, __) => Icon(Icons.error),
                data: (report) {
                  return Stack(
                    alignment: Alignment.center,
                    children: [
                      PieChart(
                        PieChartData(
                          sections: _getPieChartSections(
                            report.categoryBreakdown,
                          ),
                          centerSpaceRadius: 80,
                          sectionsSpace: 0,
                          startDegreeOffset: -90,
                        ),
                      ),
                      Column(
                        mainAxisSize: MainAxisSize.min,
                        children: [
                          Text(
                            'Total Spend',
                            style: GoogleFonts.plusJakartaSans(
                              fontSize: 10,
                              fontWeight: FontWeight.w700,
                              color: Theme.of(context).dividerColor,
                              letterSpacing: 0.5,
                            ),
                            textAlign: TextAlign.center,
                          ),
                          SizedBox(height: 2),
                          Text(
                            '₹ ${report.total.toStringAsFixed(2)}', // dynamic value later
                            style: GoogleFonts.plusJakartaSans(
                              fontSize: 24,
                              fontWeight: FontWeight.w800,
                              color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                              letterSpacing: -0.5,
                            ),
                          ),
                          // SizedBox(height: 4),
                          // Container(
                          //   padding: EdgeInsets.fromLTRB(8, 2, 8, 2),
                          //   decoration: BoxDecoration(
                          //     color: const Color(0xFFFEE2E2),
                          //     borderRadius: BorderRadius.circular(12),
                          //   ),
                          //   child: Row(
                          //     mainAxisSize: MainAxisSize.min,
                          //     children: [
                          //       Icon(
                          //         Icons.arrow_upward,
                          //         size: 12,
                          //         color: Theme.of(context).colorScheme.error,
                          //       ),
                          //       SizedBox(width: 2),
                          //       Text(
                          //         '2.4%',
                          //         style: GoogleFonts.plusJakartaSans(
                          //           fontSize: 10,
                          //           fontWeight: FontWeight.w700,
                          //           color: Theme.of(context).colorScheme.error,
                          //         ),
                          //       ),
                          //     ],
                          //   ),
                          // ),
                        ],
                      ),
                    ],
                  );
                },
              ),
            ),
          ),
          SizedBox(height: 40),
          expenseAsync.when(
            loading: () => SizedBox(),
            error: (_, __) => Text('Error loading data'),
            data: (report) => Column(
              children: report.categoryBreakdown.take(4).map((category) {
                final color = _parseColorCode(category.categoryColorCode);

                return Container(
                  margin: EdgeInsets.only(bottom: 16),
                  child: Row(
                    children: [
                      Container(
                        width: 12,
                        height: 12,
                        decoration: BoxDecoration(
                          color: color,
                          shape: BoxShape.circle,
                        ),
                      ),
                      SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          category.categoryName,
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                            color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                          ),
                        ),
                      ),
                      Text(
                        '${category.percentage.toStringAsFixed(0)}%',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey,
                        ),
                      ),
                      SizedBox(width: 16),
                      Text(
                        '₹${category.total.toStringAsFixed(0)}',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey,
                        ),
                      ),
                    ],
                  ),
                );
              }).toList(),
            ),
          ),
        ],
      ),
    );
  }

  void _navigateToAddTransaction(String type) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => TransactionFormPage()),
    );
  }

  void _navigateToTransactions() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const TransactionsPage()),
    );
  }

  void _showCategoryPopover(CategoryBreakdown category) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          contentPadding: EdgeInsets.all(20),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                children: [
                  Container(
                    width: 20,
                    height: 20,
                    decoration: BoxDecoration(
                      color: _parseColorCode(category.categoryColorCode),
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                  SizedBox(width: 12),
                  Text(
                    category.categoryName,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                      color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                    ),
                  ),
                ],
              ),
              SizedBox(height: 16),
              const Divider(),
              SizedBox(height: 16),
              _buildMerchantItem('Swiggy', '₹2,500', '5 transactions'),
              _buildMerchantItem('Zomato', '₹1,800', '3 transactions'),
              _buildMerchantItem(
                'Local Restaurant',
                '₹1,200',
                '2 transactions',
              ),
              SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF4A90E2),
                  foregroundColor: Theme.of(context).colorScheme.surface,
                  minimumSize: const Size(double.infinity, 44),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(8),
                  ),
                ),
                child: Text('View All Details'),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildMerchantItem(
    String merchant,
    String amount,
    String transactions,
  ) {
    return Container(
      margin: EdgeInsets.only(bottom: 8),
      padding: EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 16,
            backgroundColor: Theme.of(context).dividerColor,
            child: Text(
              merchant[0],
              style: GoogleFonts.plusJakartaSans(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: Theme.of(context).textTheme.bodyMedium?.color ?? Colors.black,
              ),
            ),
          ),
          SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  merchant,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                  ),
                ),
                Text(
                  transactions,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 10,
                    color: Theme.of(context).textTheme.bodyMedium?.color ?? Colors.black,
                  ),
                ),
              ],
            ),
          ),
          Text(
            amount,
            style: GoogleFonts.plusJakartaSans(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
            ),
          ),
        ],
      ),
    );
  }

  Color _parseColorCode(String? colorCode) {
    if (colorCode == null || colorCode.isEmpty) {
      return Colors.grey;
    }
    try {
      final intValue = int.tryParse(colorCode);
      if (intValue != null) {
        return Color(intValue);
      }
      
      String hexString = colorCode.replaceAll('#', '');
      if (hexString.length == 6) {
        hexString = 'FF$hexString'; // append alpha
      }
      return Color(int.parse(hexString, radix: 16));
    } catch (e) {
      return Colors.grey;
    }
  }

  List<PieChartSectionData> _getPieChartSections(
    List<CategoryBreakdown> categories,
  ) {
    return categories.map((category) {
      return PieChartSectionData(
        color: _parseColorCode(category.categoryColorCode),
        value: category.total,
        title: '$currency${category.total.toStringAsFixed(0)}',
        radius: 70,
        titleStyle: GoogleFonts.plusJakartaSans(
          fontSize: 12,
          fontWeight: FontWeight.bold,
          color: Theme.of(context).colorScheme.surface,
        ),
      );
    }).toList();
  }

  Widget _buildExpenseLegend(List<CategoryBreakdown> categories) {
    return Column(
      children: categories.map((category) {
        return _buildLegendItem(
          category,
          '${category.percentage.toStringAsFixed(1)}%',
          '$currency${category.total.toStringAsFixed(2)}',
        );
      }).toList(),
    );
  }

  Widget _buildLoadingLegend() {
    return Column(
      children: List.generate(3, (index) {
        return Container(
          margin: EdgeInsets.only(bottom: 12),
          padding: EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Container(width: 16, height: 16, color: Theme.of(context).dividerColor),
              SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(width: 100, height: 14, color: Theme.of(context).dividerColor),
                    SizedBox(height: 4),
                    Container(width: 60, height: 12, color: Theme.of(context).dividerColor),
                  ],
                ),
              ),
              Container(width: 40, height: 12, color: Theme.of(context).dividerColor),
            ],
          ),
        );
      }),
    );
  }

  Widget _buildLegendItem(
    CategoryBreakdown category,
    String percentage,
    String amount,
  ) {
    final color = _parseColorCode(category.categoryColorCode);
    return GestureDetector(
      onTap: () => _showCategoryPopover(category),
      child: Container(
        margin: EdgeInsets.only(bottom: 12),
        padding: EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.grey[50],
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Theme.of(context).dividerColor),
        ),
        child: Row(
          children: [
            Container(
              width: 16,
              height: 16,
              decoration: BoxDecoration(
                color: color,
                borderRadius: BorderRadius.circular(4),
              ),
            ),
            SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    category.categoryName,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                    ),
                  ),
                  Text(
                    amount,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      color: Theme.of(context).textTheme.bodyMedium?.color ?? Colors.black,
                    ),
                  ),
                ],
              ),
            ),
            Text(
              percentage,
              style: GoogleFonts.plusJakartaSans(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: color,
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildRecentTransactions() {
    final txAsync = ref.watch(transactionsControllerProvider);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Recent Transactions',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 18,
            fontWeight: FontWeight.w600,
            color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
          ),
        ),
        SizedBox(height: 8),
        txAsync.when(
          loading: () => SizedBox(
            height: 200,
            child: _buildLoadingTransactions(),
          ), // Keep height for loading state
          error: (_, __) =>
              SizedBox(height: 200, child: _buildErrorTransactions()),
          data: (transactions) {
            if (transactions.isEmpty) {
              return SizedBox(height: 200, child: _buildEmptyTransactions());
            }

            return ListView.builder(
              shrinkWrap: true,
              padding: EdgeInsets.zero, // Removed default padding if any
              physics: const NeverScrollableScrollPhysics(),
              itemCount: transactions.length > 10 ? 10 : transactions.length,
              itemBuilder: (_, i) => TransactionCard(
                transaction: transactions[i],
                onTap: () {},
              ),
            );
          },
        ),
      ],
    );
  }

  Widget _buildLoadingTransactions() {
    return ListView.builder(
      itemCount: 3,
      itemBuilder: (context, index) {
        return Container(
          margin: EdgeInsets.only(bottom: 8),
          padding: EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surface,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: Theme.of(context).dividerColor,
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      width: 100,
                      height: 16,
                      decoration: BoxDecoration(
                        color: Theme.of(context).dividerColor,
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                    SizedBox(height: 4),
                    Container(
                      width: 60,
                      height: 12,
                      decoration: BoxDecoration(
                        color: Theme.of(context).dividerColor,
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                width: 80,
                height: 16,
                decoration: BoxDecoration(
                  color: Theme.of(context).dividerColor,
                  borderRadius: BorderRadius.circular(4),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildErrorTransactions() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.error_outline, color: Theme.of(context).colorScheme.error.withValues(alpha: 0.5), size: 48),
          SizedBox(height: 8),
          Text(
            'Failed to load transactions',
            style: GoogleFonts.plusJakartaSans(color: Theme.of(context).colorScheme.error, fontSize: 14),
          ),
          SizedBox(height: 8),
          ElevatedButton(onPressed: _handleRefresh, child: Text('Retry')),
        ],
      ),
    );
  }

  Widget _buildEmptyTransactions() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Icon(Icons.receipt_long, color: Theme.of(context).dividerColor, size: 48),
          SizedBox(height: 8),
          Text(
            'No transactions yet',
            style: GoogleFonts.plusJakartaSans(color: Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey, fontSize: 14),
          ),
          SizedBox(height: 8),
          ElevatedButton(
            onPressed: () => _navigateToAddTransaction('expense'),
            child: Text('Add First Transaction'),
          ),
        ],
      ),
    );
  }

}
