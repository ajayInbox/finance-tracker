// lib/pages/dashboard_page.dart
import 'package:chart_sparkline/chart_sparkline.dart';
import 'package:finance_app/data/models/expense_report.dart';
import 'package:finance_app/data/models/category_breakdown.dart';
import 'package:finance_app/utils/glassmorphic_container.dart';
import 'package:finance_app/widgets/sms_modal.dart';
import 'package:flutter/material.dart';
import 'package:fl_chart/fl_chart.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:intl/intl.dart';
import 'package:finance_app/data/services/transaction_service.dart';
import 'package:finance_app/data/models/transaction_summary.dart';
import 'package:finance_app/pages/add_transaction_page.dart';
import 'package:finance_app/pages/transactions_page.dart';
import 'dart:math' as math;
import 'package:shimmer/shimmer.dart';

class DashboardPage extends StatefulWidget {
  const DashboardPage({super.key});

  @override
  State<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends State<DashboardPage> with TickerProviderStateMixin {
  late Future<List<TransactionSummary>> _recentTransactionsFuture;
  late Future<ExpenseReport> _expenseAnalysis;
  final String currency = '₹';

  // Animation controllers
  late AnimationController _fadeController;
  late AnimationController _slideController;

  // State for time period selection
  String _selectedTimePeriod = 'This Month';

  @override
  void initState() {
    super.initState();
    _recentTransactionsFuture = TransactionService().getFeed();
    _expenseAnalysis = TransactionService().fetchExpenseReport();

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

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC),
      body: RefreshIndicator(
        onRefresh: _handleRefresh,
        child: SafeArea(
          child: SingleChildScrollView(
            physics: const AlwaysScrollableScrollPhysics(),
            padding: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 8.0),
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
                    _buildTopSummaryCards2(),
                    const SizedBox(height: 24),
                    _buildQuickActionsGrid(),
                    const SizedBox(height: 12),
                    _buildInsightsCard(),
                    const SizedBox(height: 24),
                    _buildExpenseBreakdownCard(),
                    const SizedBox(height: 24),
                    _buildRecentTransactions(),
                    const SizedBox(height: 100),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _handleRefresh() async {
    setState(() {
      _recentTransactionsFuture = TransactionService().getFeed();
    });
    await Future.delayed(const Duration(milliseconds: 500));
  }

  // Enhanced Top Summary Cards with Sparklines
  Widget _buildTopSummaryCards() {
    return FutureBuilder<ExpenseReport>(
      future: _expenseAnalysis,
      builder: (context, snapshot) {
        if (snapshot.connectionState == ConnectionState.waiting) {
          return Row(
            children: [
              Expanded(child: _buildLoadingSummaryCard()),
              const SizedBox(width: 16),
              Expanded(child: _buildLoadingSummaryCard()),
            ],
          );
        }
        if (snapshot.hasError) {
          return Row(
            children: [
              Expanded(child: _buildErrorSummaryCard()),
              const SizedBox(width: 16),
              Expanded(child: _buildErrorSummaryCard()),
            ],
          );
        }
        final report = snapshot.data!;
        return Row(
          children: [
            Expanded(
              child: _buildSummaryCard(
                title: 'This Month',
                amount: '${report.currency} ${report.total.toStringAsFixed(2)}',
                trend: '+12.5%', // TODO: Calculate actual trend
                trendUp: true,
                subtitle: 'vs last month',
                gradient: const LinearGradient(
                  colors: [Color(0xFF4A90E2), Color(0xFF357ABD)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                sparklineData: [20, 35, 25, 45, 30, 40, 45], // TODO: Use real data
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: _buildSummaryCard(
                title: 'Total Balance',
                amount: '${report.currency} 1,25,750', // TODO: Fetch actual balance
                trend: '+8.2%',
                trendUp: true,
                subtitle: 'all accounts',
                gradient: const LinearGradient(
                  colors: [Color(0xFF28A745), Color(0xFF20C997)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                sparklineData: [80, 85, 90, 95, 100, 110, 125],
              ),
            ),
          ],
        );
      },
    );
  }

  Widget _buildLoadingSummaryCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.grey[200],
        borderRadius: BorderRadius.circular(20),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Container(width: 80, height: 12, color: Colors.grey[300]),
          const SizedBox(height: 12),
          Container(width: 100, height: 24, color: Colors.grey[300]),
          const SizedBox(height: 8),
          Container(width: 60, height: 12, color: Colors.grey[300]),
        ],
      ),
    );
  }

  Widget _buildErrorSummaryCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.red[50],
        borderRadius: BorderRadius.circular(20),
      ),
      child: const Center(child: Text('Error loading data')),
    );
  }

  Widget _buildSummaryCard({
    required String title,
    required String amount,
    required String trend,
    required bool trendUp,
    required String subtitle,
    required Gradient gradient,
    required List<double> sparklineData,
  }) {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        gradient: gradient,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: gradient.colors.first.withValues(alpha: 0.3),
            blurRadius: 12,
            offset: const Offset(0, 6),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                title,
                style: GoogleFonts.inter(
                  color: Colors.white70,
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                ),
              ),
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Icon(
                  title == 'This Month' ? Icons.trending_up : Icons.account_balance_wallet,
                  color: Colors.white,
                  size: 20,
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          Text(
            amount,
            style: GoogleFonts.inter(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 8),
          Row(
            children: [
              Icon(
                trendUp ? Icons.arrow_upward : Icons.arrow_downward,
                color: Colors.white,
                size: 14,
              ),
              const SizedBox(width: 4),
              Text(
                trend,
                style: GoogleFonts.inter(
                  color: Colors.white,
                  fontSize: 12,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const SizedBox(width: 6),
              Flexible(
                child: Text(
                  subtitle,
                  style: GoogleFonts.inter(
                    color: Colors.white70,
                    fontSize: 10,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12),
          SizedBox(
            height: 30,
            child: _buildSparklineChart(sparklineData, trendUp ? Colors.white : Colors.white70),
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

  // Enhanced Quick Actions Grid (2x2)
  Widget _buildQuickActionsGrid() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Quick Actions',
          style: GoogleFonts.inter(
            fontSize: 18,
            fontWeight: FontWeight.w600,
            color: Colors.grey[800],
          ),
        ),
        const SizedBox(height: 16),
        GridView.count(
          crossAxisCount: 2,
          shrinkWrap: true,
          physics: const NeverScrollableScrollPhysics(),
          mainAxisSpacing: 12,
          crossAxisSpacing: 12,
          childAspectRatio: 1.2,
          children: [
            _buildQuickActionCard(
              '📩',
              'Scan SMS',
              'Auto-detect transactions',
              const Color(0xFFE3F2FD),
              const Color(0xFF2196F3),
              () => _showSMSModal(),
            ),
            _buildQuickActionCard(
              '➕',
              'Add Expense',
              'Record new expense',
              const Color(0xFFFFEBEE),
              const Color(0xFFF44336),
              () => _navigateToAddTransaction('expense'),
            ),
            _buildQuickActionCard(
              '💰',
              'Add Income',
              'Record new income',
              const Color(0xFFE8F5E8),
              const Color(0xFF4CAF50),
              () => _navigateToAddTransaction('income'),
            ),
            _buildQuickActionCard(
              '📋',
              'View All',
              'See all transactions',
              const Color(0xFFF3E5F5),
              const Color(0xFF9C27B0),
              () => _navigateToTransactions(),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildQuickActionCard(
    String emoji,
    String title,
    String subtitle,
    Color bgColor,
    Color iconColor,
    VoidCallback onTap,
  ) {
    return GestureDetector(
      onTap: onTap,
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: bgColor,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withValues(alpha: 0.1),
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              emoji,
              style: TextStyle(fontSize: 28),
            ),
            const SizedBox(height: 8),
            Text(
              title,
              style: GoogleFonts.inter(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: Colors.grey[800],
              ),
              textAlign: TextAlign.center,
            ),
            const SizedBox(height: 4),
            Text(
              subtitle,
              style: GoogleFonts.inter(
                fontSize: 10,
                color: Colors.grey[600],
              ),
              textAlign: TextAlign.center,
            ),
          ],
        ),
      ),
    );
  }

  // Smart Insights Card
  Widget _buildInsightsCard() {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Colors.white, Colors.grey[50]!],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Colors.grey[200]!),
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: const BoxDecoration(
              color: Color(0xFFFFF3CD),
              shape: BoxShape.circle,
            ),
            child: const Text('💡', style: TextStyle(fontSize: 20)),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Smart Insights',
                  style: GoogleFonts.inter(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: Colors.grey[800],
                  ),
                ),
                const SizedBox(height: 2),
                Text(
                  'You spent 20% more on Food this week than last week.',
                  style: GoogleFonts.inter(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),
          Icon(Icons.arrow_forward_ios, size: 16, color: Colors.grey[400]),
        ],
      ),
    );
  }

  void _showSMSModal() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) {
        return SMSModal();
      },
    );
  }

  void _navigateToAddTransaction(String type) {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const AddTransactionPage()),
    );
  }

  void _navigateToTransactions() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const TransactionsPage()),
    );
  }

  Widget _buildExpenseBreakdownCard() {
    return Container(
      padding: const EdgeInsets.all(20),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(20),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withValues(alpha: 0.1),
            spreadRadius: 1,
            blurRadius: 12,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Expanded(
                child: Text(
                  'Expense Breakdown',
                  style: GoogleFonts.inter(
                    fontSize: 18,
                    fontWeight: FontWeight.w600,
                    color: Colors.grey[800],
                  ),
                ),
              ),
              // Time Period Toggle
              Flexible(
                child: Container(
                  decoration: BoxDecoration(
                    color: Colors.grey[100],
                    borderRadius: BorderRadius.circular(20),
                  ),
                  child: Row(
                    children: [
                      Flexible(child: _buildTimePeriodButton('This Month')),
                      Flexible(child: _buildTimePeriodButton('Last Month')),
                      Flexible(child: _buildTimePeriodButton('Custom')),
                    ],
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 30),
          FutureBuilder<ExpenseReport>(
            future: _expenseAnalysis,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return SizedBox(
                  height: 200,
                  child: Center(child: CircularProgressIndicator()),
                );
              }
              if (snapshot.hasError) {
                return SizedBox(
                  height: 200,
                  child: Center(child: Text('Error loading chart data')),
                );
              }
              final report = snapshot.data!;
              return SizedBox(
                height: 200,
                child: PieChart(
                  PieChartData(
                    sections: _getPieChartSections(report.categoryBreakdown),
                    centerSpaceRadius: 60,
                    sectionsSpace: 3,
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 35),
          FutureBuilder<ExpenseReport>(
            future: _expenseAnalysis,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return _buildLoadingLegend();
              }
              if (snapshot.hasError) {
                return Text('Error loading legend');
              }
              final report = snapshot.data!;
              return _buildExpenseLegend(report.categoryBreakdown);
            },
          ),
        ],
      ),
    );
  }

  Widget _buildTimePeriodButton(String period) {
    final isSelected = _selectedTimePeriod == period;

    return GestureDetector(
      onTap: () {
        setState(() {
          _selectedTimePeriod = period;
        });
      },
      child: AnimatedContainer(
        duration: const Duration(milliseconds: 200),
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
        margin: const EdgeInsets.all(2),
        decoration: BoxDecoration(
          gradient: isSelected
              ? const LinearGradient(
                  colors: [Color(0xFF4A90E2), Color(0xFF357ABD)],
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                )
              : null,
          color: isSelected ? null : Colors.transparent,
          borderRadius: BorderRadius.circular(16),
        ),
        child: Text(
          period,
          style: GoogleFonts.inter(
            fontSize: 11,
            fontWeight: FontWeight.w500,
            color: isSelected ? Colors.white : Colors.grey[600],
          ),
        ),
      ),
    );
  }

  void _showCategoryPopover(String category) {
    showDialog(
      context: context,
      builder: (context) {
        return AlertDialog(
          shape: RoundedRectangleBorder(borderRadius: BorderRadius.circular(16)),
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
              _buildMerchantItem('Local Restaurant', '₹1,200', '2 transactions'),
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

  Widget _buildMerchantItem(String merchant, String amount, String transactions) {
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

  List<PieChartSectionData> _getPieChartSections(List<CategoryBreakdown> categories) {
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

  Widget _buildLegendItem(Color color, String category, String percentage, String amount) {
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
                style: GoogleFonts.inter(
                  color: Colors.blue,
                  fontSize: 14,
                  fontWeight: FontWeight.w500,
                ),
              ),
            ),
          ],
        ),
        const SizedBox(height: 16),
        SizedBox(
          height: 200,
          child: FutureBuilder<List<TransactionSummary>>(
            future: _recentTransactionsFuture,
            builder: (context, snapshot) {
              if (snapshot.connectionState == ConnectionState.waiting) {
                return _buildLoadingTransactions();
              }

              if (snapshot.hasError) {
                return _buildErrorTransactions();
              }

              final transactions = snapshot.data ?? [];
              if (transactions.isEmpty) {
                return _buildEmptyTransactions();
              }

              return ListView.builder(
                itemCount: transactions.length > 5 ? 5 : transactions.length,
                itemBuilder: (context, index) {
                  final transaction = transactions[index];
                  return _buildTransactionItem(transaction);
                },
              );
            },
          ),
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
            style: GoogleFonts.inter(
              color: Colors.red[600],
              fontSize: 14,
            ),
          ),
          const SizedBox(height: 8),
          ElevatedButton(
            onPressed: _handleRefresh,
            child: Text('Retry'),
          ),
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
            style: GoogleFonts.inter(
              color: Colors.grey[500],
              fontSize: 14,
            ),
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
    return Container(
      margin: const EdgeInsets.only(bottom: 8),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withValues(alpha: 0.1),
            spreadRadius: 1,
            blurRadius: 2,
            offset: const Offset(0, 1),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: transaction.type.toLowerCase() == "income"
                  ? Colors.green.withValues(alpha: 0.1)
                  : Colors.red.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(10),
            ),
            child: Icon(
              transaction.type.toLowerCase() == "income"
                  ? Icons.arrow_upward
                  : Icons.arrow_downward,
              color: transaction.type.toLowerCase() == "income"
                  ? Colors.green
                  : Colors.red,
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
                  style: GoogleFonts.inter(
                    fontWeight: FontWeight.w600,
                    fontSize: 14,
                    color: Colors.grey[800],
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
                const SizedBox(height: 2),
                Text(
                  transaction.categoryName,
                  style: GoogleFonts.inter(
                    color: Colors.grey[600],
                    fontSize: 12,
                  ),
                  maxLines: 1,
                  overflow: TextOverflow.ellipsis,
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '${transaction.type.toLowerCase() == "income" ? "+" : "-"}₹${transaction.amount.abs()}',
                style: GoogleFonts.inter(
                  color: transaction.type.toLowerCase() == "income"
                      ? Colors.green
                      : Colors.red,
                  fontWeight: FontWeight.bold,
                  fontSize: 14,
                ),
              ),
              const SizedBox(height: 2),
              Text(
                DateFormat('MMM d').format(transaction.occuredAt),
                style: GoogleFonts.inter(
                  color: Colors.grey[500],
                  fontSize: 10,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

Widget _buildLoadingSummaryCard2() {
  return Shimmer.fromColors(
    baseColor: Colors.grey[800]!,
    highlightColor: Colors.grey[700]!,
    child: Container(
      height: 160, // Match the height of your actual summary card
      decoration: BoxDecoration(
        color: Colors.black.withOpacity(0.2),
        borderRadius: BorderRadius.circular(24.0),
      ),
    ),
  );
}

// Your _buildSummaryCard implementation needs to be updated to include the sparkline
Widget _buildSummaryCard2({
  required String title,
  required String amount,
  required String trend,
  required bool trendUp,
  required String subtitle,
  required Gradient gradient,
  required List<double> sparklineData,
}) {
  return GlassmorphicContainer(
    gradient: gradient,
    child: Padding(
      padding: const EdgeInsets.all(16.0),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(title, style: const TextStyle(color: Colors.white, fontSize: 16)),
          const SizedBox(height: 8),
          Text(amount, style: const TextStyle(color: Colors.white, fontSize: 28, fontWeight: FontWeight.bold)),
          const SizedBox(height: 8),
          Row(
            children: [
              Icon(trendUp ? Icons.arrow_upward : Icons.arrow_downward, color: trendUp ? Colors.greenAccent : Colors.redAccent, size: 16),
              const SizedBox(width: 4),
              Text(trend, style: TextStyle(color: trendUp ? Colors.greenAccent : Colors.redAccent)),
              const SizedBox(width: 4),
              Text(subtitle, style: TextStyle(color: Colors.white.withOpacity(0.7))),
            ],
          ),
          const Spacer(),
          SizedBox(
            height: 30,
            child: Sparkline(
              data: sparklineData,
              lineColor: Colors.white.withOpacity(0.8),
              pointsMode: PointsMode.none,
            ),
          ),
        ],
      ),
    ),
  );
}

// Your main widget, now refactored
Widget _buildTopSummaryCards2() {
  return FutureBuilder<ExpenseReport>(
    future: _expenseAnalysis,
    builder: (context, snapshot) {
      if (snapshot.connectionState == ConnectionState.waiting) {
        return Row(
          children: [
            Expanded(child: _buildLoadingSummaryCard()),
            const SizedBox(width: 16),
            Expanded(child: _buildLoadingSummaryCard()),
          ],
        );
      }
      if (snapshot.hasError) {
        // You can create a more descriptive error card
        return const Center(child: Text('Failed to load data', style: TextStyle(color: Colors.white)));
      }
      final report = snapshot.data!;
      return SizedBox(
        height: 200, // Give the row a fixed height to ensure cards are the same size
        child: Row(
          children: [
            Expanded(
              child: _buildSummaryCard(
                title: 'This Month',
                amount: '₹ ${report.total.toStringAsFixed(2)}',
                trend: '+12.5%', // TODO: Calculate actual trend
                trendUp: true,
                subtitle: 'vs last month',
                gradient: const LinearGradient(
                  colors: [Color(0xFF6A11CB), Color(0xFF2575FC)], // Modern purple-to-blue gradient
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                sparklineData: [20, 35, 25, 45, 30, 40, 45].map((e) => e.toDouble()).toList(), // TODO: Use real data
              ),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: _buildSummaryCard(
                title: 'Total Balance',
                amount: '₹ 1,25,750', // TODO: Fetch actual balance
                trend: '+8.2%',
                trendUp: true,
                subtitle: 'all accounts',
                gradient: const LinearGradient(
                  colors: [Color(0xFF20BF55), Color(0xFF01BAEF)], // Modern green-to-cyan gradient
                  begin: Alignment.topLeft,
                  end: Alignment.bottomRight,
                ),
                sparklineData: [80, 85, 90, 95, 100, 110, 125].map((e) => e.toDouble()).toList(),
              ),
            ),
          ],
        ),
      );
    },
  );
}


}