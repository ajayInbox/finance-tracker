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
import 'package:intl/intl.dart';

import 'package:finance_app/features/transaction/ui/transactions_page.dart';
import 'dart:math' as math;

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
              padding: const EdgeInsets.fromLTRB(
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
                      _buildHeader(), // New Header
                      const SizedBox(height: 24),
                      _buildTopSummaryCards(),
                      const SizedBox(height: 24),
                      _buildSpendingAnalysisCard(),
                      const SizedBox(height: 24),
                      _buildRecentTransactions(), // Preserved
                      const SizedBox(height: 24),
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

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.only(top: 48, bottom: 20),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                decoration: BoxDecoration(
                  shape: BoxShape.circle,
                  border: Border.all(color: const Color(0xFF10B981), width: 2),
                ),
                padding: const EdgeInsets.all(2),
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
                      color: Colors.grey[300],
                      child: const Icon(Icons.person, color: Colors.grey),
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 16),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'WELCOME BACK',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      fontWeight: FontWeight.w600,
                      color: Colors.grey[500],
                      letterSpacing: 1.0,
                    ),
                  ),
                  Text(
                    'Alex Johnson',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: const Color(0xFF111827),
                    ),
                  ),
                ],
              ),
            ],
          ),
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withOpacity(0.05),
                  blurRadius: 10,
                  offset: const Offset(0, 2),
                ),
              ],
            ),
            child: Stack(
              children: [
                const Center(
                  child: Icon(
                    Icons.notifications_none_rounded,
                    color: Color(0xFF4B5563),
                    size: 24,
                  ),
                ),
                Positioned(
                  top: 12,
                  right: 14,
                  child: Container(
                    width: 8,
                    height: 8,
                    decoration: const BoxDecoration(
                      color: Color(0xFFEF4444),
                      shape: BoxShape.circle,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFloatingActionButton() {
    return Padding(
      padding: const EdgeInsets.only(bottom: 80),
      child: SizedBox(
        width: 56,
        height: 56,
        child: FloatingActionButton(
          backgroundColor: const Color(0xFF10B981), // primary
          elevation: 8, // shadow-glow approx
          shape: const CircleBorder(),
          onPressed: () => _openTransactionForm(null),
          child: const Icon(Icons.add, color: Colors.white, size: 28),
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
          backgroundColor: Colors.red,
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
      const SizedBox(width: 16),
      Expanded(child: _buildLoadingSummaryCard()),
    ],
  );

  Widget _buildTopError() => Row(
    children: [
      Expanded(child: _buildErrorSummaryCard()),
      const SizedBox(width: 16),
      Expanded(child: _buildErrorSummaryCard()),
    ],
  );

  Widget _buildLoadingSummaryCard() {
    return Container(
      height: 180,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(24),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 80, height: 12, color: Colors.grey[100]),
          const SizedBox(height: 12),
          Container(width: 100, height: 24, color: Colors.grey[100]),
        ],
      ),
    );
  }

  Widget _buildErrorSummaryCard() {
    return Container(
      height: 180,
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.red[50],
        borderRadius: BorderRadius.circular(24),
      ),
      child: const Center(child: Text('Error loading data')),
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
            color: const Color(0xFFEF4444), // Red 500
            bgColor: const Color(0xFFEF4444),
            glowColor: const Color(0xFFEF4444),
          ),
        ),
        const SizedBox(width: 16),
        Expanded(
          child: _buildGlowCard(
            title: 'Total Balance',
            amount: networth.formattedNetWorth,
            trend: '+12%',
            trendUp: true,
            icon: Icons.account_balance_wallet,
            color: const Color(0xFF10B981),
            bgColor: const Color(0xFF10B981),
            glowColor: const Color(0xFF10B981),
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
        color: Colors.white,
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
            padding: const EdgeInsets.all(20),
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
                    const SizedBox(width: 12), // gap-3 = 12px
                    Expanded(
                      child: Text(
                        title,
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14, // text-sm
                          fontWeight: FontWeight.w500, // font-medium
                          color: Colors.grey[500], // text-secondary
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
                          color: const Color(0xFF111827),
                          letterSpacing: -0.5, // tracking-tight
                        ),
                      ),
                    ),
                    const SizedBox(
                      height: 12,
                    ), // mb-3 = 12px margin bottom on h2
                    Container(
                      padding: const EdgeInsets.symmetric(
                        horizontal: 12, // px-3
                        vertical: 4, // py-1
                      ),
                      decoration: BoxDecoration(
                        color: trendUp
                            ? const Color(0xFFECFDF5)
                            : const Color(0xFFFEF2F2), // green-50 : red-50
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
                                ? const Color(0xFF059669)
                                : const Color(
                                    0xFFDC2626,
                                  ), // green-600 : red-600
                          ),
                          const SizedBox(width: 4), // gap-1
                          Text(
                            trend,
                            style: GoogleFonts.plusJakartaSans(
                              fontSize: 12, // text-xs
                              fontWeight: FontWeight.w700, // font-bold
                              color: trendUp
                                  ? const Color(0xFF047857)
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
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(40), // rounded-[2.5rem]
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
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
                      color: const Color(0xFF111827),
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    'Monthly Breakdown',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 14,
                      color: Colors.grey[500],
                    ),
                  ),
                ],
              ),
              IconButton(
                icon: const Icon(Icons.more_horiz, color: Colors.grey),
                onPressed: () {},
              ),
            ],
          ),
          const SizedBox(height: 24),
          // Time period selector
          Container(
            padding: const EdgeInsets.all(4),
            decoration: BoxDecoration(
              color: const Color(0xFFF3F4F6), // gray-100
              borderRadius: BorderRadius.circular(16),
            ),
            child: Row(
              children: [
                Expanded(
                  child: Container(
                    padding: const EdgeInsets.symmetric(vertical: 8),
                    decoration: BoxDecoration(
                      color: Colors.white,
                      borderRadius: BorderRadius.circular(12),
                      boxShadow: [
                        BoxShadow(
                          color: Colors.black.withOpacity(0.05),
                          blurRadius: 4,
                          offset: const Offset(0, 1),
                        ),
                      ],
                    ),
                    child: Center(
                      child: Text(
                        '1W',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14,
                          fontWeight: FontWeight.w700,
                          color: const Color(0xFF111827),
                        ),
                      ),
                    ),
                  ),
                ),
                for (var period in ['1M', '3M', '6M', '1Y']) ...[
                  const SizedBox(width: 4),
                  Expanded(
                    child: Center(
                      child: Text(
                        period,
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14,
                          fontWeight: FontWeight.w500,
                          color: Colors.grey[500],
                        ),
                      ),
                    ),
                  ),
                ],
              ],
            ),
          ),
          const SizedBox(height: 40),
          // Pie chart
          Center(
            child: SizedBox(
              height: 250,
              width: 250,
              child: Stack(
                alignment: Alignment.center,
                children: [
                  expenseAsync.when(
                    loading: () => const CircularProgressIndicator(),
                    error: (_, __) => const Icon(Icons.error),
                    data: (report) {
                      return PieChart(
                        PieChartData(
                          sections: _getPieChartSections(
                            report.categoryBreakdown,
                          ),
                          centerSpaceRadius: 80,
                          sectionsSpace: 0,
                          startDegreeOffset: -90,
                        ),
                      );
                    },
                  ),
                  Column(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Text(
                        'Total Spend',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 10,
                          fontWeight: FontWeight.w700,
                          color: Colors.grey[400],
                          letterSpacing: 0.5,
                        ),
                        textAlign: TextAlign.center,
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '₹2,100', // dynamic value later
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 24,
                          fontWeight: FontWeight.w800,
                          color: const Color(0xFF111827),
                          letterSpacing: -0.5,
                        ),
                      ),
                      const SizedBox(height: 4),
                      Container(
                        padding: const EdgeInsets.fromLTRB(8, 2, 8, 2),
                        decoration: BoxDecoration(
                          color: const Color(0xFFFEE2E2),
                          borderRadius: BorderRadius.circular(12),
                        ),
                        child: Row(
                          mainAxisSize: MainAxisSize.min,
                          children: [
                            const Icon(
                              Icons.arrow_upward,
                              size: 12,
                              color: Color(0xFFEF4444),
                            ),
                            const SizedBox(width: 2),
                            Text(
                              '2.4%',
                              style: GoogleFonts.plusJakartaSans(
                                fontSize: 10,
                                fontWeight: FontWeight.w700,
                                color: const Color(0xFFEF4444),
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
          ),
          const SizedBox(height: 40),
          // Legend (Simplified / Not fully visible in screenshot but assuming standard list)
          // ... keeping existing legend logic but updating font
          expenseAsync.when(
            loading: () => const SizedBox(),
            error: (_, __) => const Text('Error loading data'),
            data: (report) => Column(
              children: report.categoryBreakdown.take(4).map((category) {
                final colors = [
                  const Color(0xFF10B981), // Green
                  const Color(0xFFFCA5A5), // Red/Pink
                  const Color(0xFF9CA3AF), // Grey
                  // Add more or cycle
                ];
                // basic cycle color logic similar to before, refined colors
                final color =
                    colors[report.categoryBreakdown.indexOf(category) %
                        colors.length];

                return Container(
                  margin: const EdgeInsets.only(bottom: 16),
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
                      const SizedBox(width: 12),
                      Expanded(
                        child: Text(
                          category.categoryName,
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 14,
                            fontWeight: FontWeight.w600,
                            color: const Color(0xFF111827),
                          ),
                        ),
                      ),
                      Text(
                        '${category.percentage.toStringAsFixed(0)}%',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: Colors.grey[500],
                        ),
                      ),
                      const SizedBox(width: 16),
                      Text(
                        '₹${category.total.toStringAsFixed(0)}',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 14,
                          fontWeight: FontWeight.w600,
                          color: Colors.grey[500],
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

  void _showCategoryPopover(String category) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          contentPadding: const EdgeInsets.all(20),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Row(
                children: [
                  Container(
                    width: 20,
                    height: 20,
                    decoration: BoxDecoration(
                      color: _getCategoryColor(category),
                      borderRadius: BorderRadius.circular(4),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Text(
                    category,
                    style: GoogleFonts.inter(
                      fontSize: 18,
                      fontWeight: FontWeight.w600,
                      color: Colors.grey[800],
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              const Divider(),
              const SizedBox(height: 16),
              _buildMerchantItem('Swiggy', '₹2,500', '5 transactions'),
              _buildMerchantItem('Zomato', '₹1,800', '3 transactions'),
              _buildMerchantItem(
                'Local Restaurant',
                '₹1,200',
                '2 transactions',
              ),
              const SizedBox(height: 16),
              ElevatedButton(
                onPressed: () => Navigator.pop(context),
                style: ElevatedButton.styleFrom(
                  backgroundColor: const Color(0xFF4A90E2),
                  foregroundColor: Colors.white,
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
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(8),
      ),
      child: Row(
        children: [
          CircleAvatar(
            radius: 16,
            backgroundColor: Colors.grey[200],
            child: Text(
              merchant[0],
              style: GoogleFonts.inter(
                fontSize: 12,
                fontWeight: FontWeight.w600,
                color: Colors.grey[600],
              ),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  merchant,
                  style: GoogleFonts.inter(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: Colors.grey[800],
                  ),
                ),
                Text(
                  transactions,
                  style: GoogleFonts.inter(
                    fontSize: 10,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),
          Text(
            amount,
            style: GoogleFonts.inter(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Colors.grey[800],
            ),
          ),
        ],
      ),
    );
  }

  Color _getCategoryColor(String category) {
    switch (category) {
      case 'Food & Dining':
        return const Color(0xFF4A90E2);
      case 'Transport':
        return const Color(0xFF28A745);
      case 'Shopping':
        return const Color(0xFFFFA726);
      case 'Others':
        return const Color(0xFFAB47BC);
      default:
        return Colors.grey;
    }
  }

  List<PieChartSectionData> _getPieChartSections(
    List<CategoryBreakdown> categories,
  ) {
    return categories.map((category) {
      return PieChartSectionData(
        color: _getCategoryColor(category.categoryName),
        value: category.total,
        title: '$currency${category.total.toStringAsFixed(0)}',
        radius: 70,
        titleStyle: GoogleFonts.inter(
          fontSize: 12,
          fontWeight: FontWeight.bold,
          color: Colors.white,
        ),
      );
    }).toList();
  }

  Widget _buildExpenseLegend(List<CategoryBreakdown> categories) {
    return Column(
      children: categories.map((category) {
        return _buildLegendItem(
          _getCategoryColor(category.categoryName),
          category.categoryName,
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
          margin: const EdgeInsets.only(bottom: 12),
          padding: const EdgeInsets.all(12),
          decoration: BoxDecoration(
            color: Colors.grey[50],
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Container(width: 16, height: 16, color: Colors.grey[300]),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(width: 100, height: 14, color: Colors.grey[300]),
                    const SizedBox(height: 4),
                    Container(width: 60, height: 12, color: Colors.grey[300]),
                  ],
                ),
              ),
              Container(width: 40, height: 12, color: Colors.grey[300]),
            ],
          ),
        );
      }),
    );
  }

  Widget _buildLegendItem(
    Color color,
    String category,
    String percentage,
    String amount,
  ) {
    return GestureDetector(
      onTap: () => _showCategoryPopover(category),
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: Colors.grey[50],
          borderRadius: BorderRadius.circular(12),
          border: Border.all(color: Colors.grey[200]!),
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
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    category,
                    style: GoogleFonts.inter(
                      fontSize: 14,
                      fontWeight: FontWeight.w500,
                      color: Colors.grey[800],
                    ),
                  ),
                  Text(
                    amount,
                    style: GoogleFonts.inter(
                      fontSize: 12,
                      color: Colors.grey[600],
                    ),
                  ),
                ],
              ),
            ),
            Text(
              percentage,
              style: GoogleFonts.inter(
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
        Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Text(
              'Recent Transactions',
              style: GoogleFonts.inter(
                fontSize: 18,
                fontWeight: FontWeight.w600,
                color: Colors.grey[800],
              ),
            ),
            TextButton(
              onPressed: () => _navigateToTransactions(),
              child: Text(
                'See All',
                style: GoogleFonts.manrope(
                  color: const Color(0xFF13EC5B),
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 8),
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
              itemCount: transactions.length > 5 ? 5 : transactions.length,
              itemBuilder: (_, i) => _buildTransactionItem(transactions[i]),
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
          margin: const EdgeInsets.only(bottom: 8),
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(12),
          ),
          child: Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: Colors.grey[200],
                  borderRadius: BorderRadius.circular(8),
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      width: 100,
                      height: 16,
                      decoration: BoxDecoration(
                        color: Colors.grey[200],
                        borderRadius: BorderRadius.circular(4),
                      ),
                    ),
                    const SizedBox(height: 4),
                    Container(
                      width: 60,
                      height: 12,
                      decoration: BoxDecoration(
                        color: Colors.grey[200],
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
                  color: Colors.grey[200],
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
          Icon(Icons.error_outline, color: Colors.red[300], size: 48),
          const SizedBox(height: 8),
          Text(
            'Failed to load transactions',
            style: GoogleFonts.inter(color: Colors.red[600], fontSize: 14),
          ),
          const SizedBox(height: 8),
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
          Icon(Icons.receipt_long, color: Colors.grey[300], size: 48),
          const SizedBox(height: 8),
          Text(
            'No transactions yet',
            style: GoogleFonts.inter(color: Colors.grey[500], fontSize: 14),
          ),
          const SizedBox(height: 8),
          ElevatedButton(
            onPressed: () => _navigateToAddTransaction('expense'),
            child: Text('Add First Transaction'),
          ),
        ],
      ),
    );
  }

  Widget _buildTransactionItem(TransactionSummary transaction) {
    final isIncome = transaction.type.toLowerCase() == "income";
    final iconColor = isIncome ? const Color(0xFF13EC5B) : Colors.red;
    final bgColor = isIncome
        ? const Color(0xFF13EC5B).withValues(alpha: 0.1)
        : Colors.red.withValues(alpha: 0.1);

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: () {},
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: const EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: const Color(0xFFE5E7EB)),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.04),
                blurRadius: 8,
                offset: const Offset(0, 2),
              ),
            ],
          ),
          child: Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: bgColor,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  _getTransactionIcon(transaction.categoryName),
                  color: iconColor,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      transaction.transactionName,
                      style: GoogleFonts.manrope(
                        fontWeight: FontWeight.w600,
                        fontSize: 14,
                        color: const Color(0xFF0F172A), // Slate 900
                      ),
                      maxLines: 1,
                      overflow: TextOverflow.ellipsis,
                    ),
                    const SizedBox(height: 2),
                    Text(
                      _formatDate(transaction.occurredAt),
                      style: GoogleFonts.manrope(
                        color: const Color(0xFF64748B), // Slate 500
                        fontSize: 10,
                      ),
                    ),
                  ],
                ),
              ),
              Text(
                '${isIncome ? "+" : "-"}₹${transaction.amount.abs().toStringAsFixed(2)}',
                style: GoogleFonts.manrope(
                  color: iconColor,
                  fontWeight: FontWeight.w700,
                  fontSize: 14,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  IconData _getTransactionIcon(String category) {
    switch (category.toLowerCase()) {
      case 'food & dining':
        return Icons.restaurant;
      case 'shopping':
        return Icons.shopping_bag;
      case 'transport':
        return Icons.directions_car;
      case 'entertainment':
        return Icons.movie;
      case 'income':
      case 'salary':
        return Icons.trending_up;
      default:
        return Icons.receipt;
    }
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date);

    if (difference.inDays == 0) {
      return 'Today, ${DateFormat('h:mm a').format(date)}';
    } else if (difference.inDays == 1) {
      return 'Yesterday';
    } else if (difference.inDays < 7) {
      return DateFormat('EEEE').format(date);
    } else {
      return DateFormat('MMM d').format(date);
    }
  }
}
