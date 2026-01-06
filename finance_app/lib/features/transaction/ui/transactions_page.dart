// lib/pages/transactions_page.dart
import 'package:finance_app/features/transaction/application/transaction_controller.dart';
import 'package:finance_app/features/transaction/data/model/transaction_result.dart';
import 'package:finance_app/features/transaction/ui/transaction_form_page.dart';
import 'package:finance_app/utils/category_icon.dart';
import 'package:finance_app/widgets/filter_bottom_sheet.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';

class TransactionsPage extends ConsumerStatefulWidget {
  const TransactionsPage({super.key});

  @override
  ConsumerState<TransactionsPage> createState() => _TransactionsPageState();
}

class _TransactionsPageState extends ConsumerState<TransactionsPage>
    with TickerProviderStateMixin {
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';

  // Filter state
  String _selectedTimeFilter = 'All';
  String _selectedAccountFilter = 'All';
  String _selectedCategoryFilter = 'All';

  // UI state
  ScrollController _scrollController = ScrollController();

  // Animation controllers
  late AnimationController _fabAnimationController;
  late AnimationController _searchAnimationController;

  @override
  void initState() {
    super.initState();

    _fabAnimationController = AnimationController(
      duration: const Duration(milliseconds: 200),
      vsync: this,
    );

    _searchAnimationController = AnimationController(
      duration: const Duration(milliseconds: 250),
      vsync: this,
    );

    _scrollController.addListener(_onScroll);
  }

  @override
  void dispose() {
    _searchController.dispose();
    _fabAnimationController.dispose();
    _searchAnimationController.dispose();
    _scrollController.dispose();
    super.dispose();
  }

  void _onScroll() {
    if (_scrollController.offset > 100) {
      _fabAnimationController.reverse();
    } else {
      _fabAnimationController.forward();
    }
  }

  Future<void> _refreshTransactions() async {
    await ref.read(transactionsControllerProvider.notifier).refresh();
  }

  List<TransactionSummary> _filterTransactions(
    List<TransactionSummary> transactions,
  ) {
    return transactions.where((transaction) {
      // Search filter
      final matchesSearch =
          _searchQuery.isEmpty ||
          transaction.transactionName.toLowerCase().contains(
            _searchQuery.toLowerCase(),
          ) ||
          transaction.categoryName.toLowerCase().contains(
            _searchQuery.toLowerCase(),
          ) ||
          transaction.amount.toString().contains(_searchQuery);

      // Time filter (simplified for now)
      final matchesTime = _selectedTimeFilter == 'All';

      // Account filter
      final matchesAccount =
          _selectedAccountFilter == 'All' ||
          transaction.accountName.toLowerCase().contains(
            _selectedAccountFilter.toLowerCase(),
          );

      // Category filter
      final matchesCategory =
          _selectedCategoryFilter == 'All' ||
          transaction.categoryName.toLowerCase().contains(
            _selectedCategoryFilter.toLowerCase(),
          );

      return matchesSearch && matchesTime && matchesAccount && matchesCategory;
    }).toList();
  }

  Map<String, List<TransactionSummary>> _groupTransactions(
    List<TransactionSummary> transactions,
  ) {
    final grouped = <String, List<TransactionSummary>>{};
    final now = DateTime.now();
    final today = DateTime(now.year, now.month, now.day);
    final yesterday = today.subtract(const Duration(days: 1));

    // Sort by date descending first
    transactions.sort((a, b) => b.occurredAt.compareTo(a.occurredAt));

    for (var tx in transactions) {
      final date = DateTime(
        tx.occurredAt.year,
        tx.occurredAt.month,
        tx.occurredAt.day,
      );
      String key;
      if (date == today) {
        key = 'Today';
      } else if (date == yesterday) {
        key = 'Yesterday';
      } else {
        key = DateFormat('MMM d, y').format(date);
      }

      if (!grouped.containsKey(key)) {
        grouped[key] = [];
      }
      grouped[key]!.add(tx);
    }
    return grouped;
  }

  void _showFilterDialog() {
    showModalBottomSheet(
      context: context,
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => DraggableScrollableSheet(
        expand: false,
        initialChildSize: 0.7,
        maxChildSize: 0.9,
        builder: (context, scrollController) => FilterBottomSheet(
          selectedTimeFilter: _selectedTimeFilter,
          selectedAccountFilter: _selectedAccountFilter,
          selectedCategoryFilter: _selectedCategoryFilter,
          onFiltersChanged: (time, account, category) {
            setState(() {
              _selectedTimeFilter = time;
              _selectedAccountFilter = account;
              _selectedCategoryFilter = category;
            });
          },
          onClearAll: () {
            setState(() {
              _selectedTimeFilter = 'All';
              _selectedAccountFilter = 'All';
              _selectedCategoryFilter = 'All';
            });
          },
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final transactionsAsync = ref.watch(transactionsControllerProvider);

    return Scaffold(
      backgroundColor: const Color(0xFFF3F4F6), // background-light
      body: Column(
        children: [
          _buildHeader(),
          _buildSearchBar(),
          _buildFilters(),
          Expanded(
            child: transactionsAsync.when(
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('Error: $e')),
              data: (items) {
                final filteredItems = _filterTransactions(items);
                if (filteredItems.isEmpty) {
                  return Center(
                    child: Text(
                      'No transactions found',
                      style: GoogleFonts.plusJakartaSans(
                        color: Colors.grey[500],
                        fontSize: 16,
                      ),
                    ),
                  );
                }

                final grouped = _groupTransactions(filteredItems);

                return RefreshIndicator(
                  onRefresh: _refreshTransactions,
                  child: ListView.builder(
                    padding: const EdgeInsets.fromLTRB(24, 8, 24, 100),
                    itemCount: grouped.length,
                    itemBuilder: (_, index) {
                      final key = grouped.keys.elementAt(index);
                      final transactions = grouped[key]!;

                      return Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Padding(
                            padding: const EdgeInsets.symmetric(vertical: 12),
                            child: Text(
                              key.toUpperCase(),
                              style: GoogleFonts.plusJakartaSans(
                                fontSize: 12,
                                fontWeight: FontWeight.w600,
                                color: const Color(
                                  0xFF6B7280,
                                ), // text-secondary-light
                                letterSpacing: 1.0,
                              ),
                            ),
                          ),
                          ...transactions.map(
                            (tx) => GestureDetector(
                              onTap: () => _openTransactionForm(tx),
                              child: _buildTransactionCard(tx),
                            ),
                          ),
                        ],
                      );
                    },
                  ),
                );
              },
            ),
          ),
        ],
      ),
      floatingActionButton: Padding(
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
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.only(left: 24, right: 24, top: 48, bottom: 24),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            'Transactions',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 24, // text-2xl
              fontWeight: FontWeight.w700, // font-bold
              color: const Color(0xFF111827), // text-primary-light
            ),
          ),
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16), // rounded-2xl
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.05),
                  blurRadius: 2,
                  offset: const Offset(0, 1),
                ),
              ],
            ),
            child: Stack(
              children: [
                const Center(
                  child: Icon(
                    Icons.notifications_none_rounded,
                    color: Color(0xFF4B5563), // text-gray-600
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
                      color: Color(0xFFEF4444), // bg-red-500
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

  Widget _buildSearchBar() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 12),
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(32), // rounded-2xl
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.04), // shadow-soft
              blurRadius: 40,
              offset: const Offset(0, 10),
            ),
          ],
        ),
        child: TextField(
          controller: _searchController,
          onChanged: (value) => setState(() => _searchQuery = value),
          decoration: InputDecoration(
            hintText: 'Search transactions',
            hintStyle: GoogleFonts.plusJakartaSans(
              color: Colors.grey[400],
              fontSize: 14,
              fontWeight: FontWeight.w500,
            ),
            prefixIcon: Icon(Icons.search, color: Colors.grey[400]),
            border: InputBorder.none,
            contentPadding: const EdgeInsets.symmetric(
              horizontal: 16,
              vertical: 14,
            ),
          ),
          style: GoogleFonts.plusJakartaSans(
            color: const Color(0xFF111827),
            fontSize: 14,
            fontWeight: FontWeight.w600,
          ),
        ),
      ),
    );
  }

  Widget _buildFilters() {
    return SingleChildScrollView(
      scrollDirection: Axis.horizontal,
      padding: const EdgeInsets.fromLTRB(24, 0, 24, 16),
      child: Row(
        children: [
          _buildFilterChip(
            'All',
            _selectedTimeFilter == 'All',
            () => setState(() => _selectedTimeFilter = 'All'),
          ),
          const SizedBox(width: 8),
          _buildFilterChip('Income', false, () {}),
          const SizedBox(width: 8),
          _buildFilterChip('Expense', false, () {}),
          const SizedBox(width: 8),
          GestureDetector(
            onTap: _showFilterDialog,
            child: Container(
              padding: const EdgeInsets.all(10),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(12),
                border: Border.all(color: Colors.grey[200]!),
                boxShadow: [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.05),
                    blurRadius: 2,
                    offset: const Offset(0, 1),
                  ),
                ],
              ),
              child: Icon(Icons.tune, color: Colors.grey[500], size: 20),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFilterChip(
    String label,
    bool isSelected,
    VoidCallback onTap, {
    IconData? icon,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: isSelected ? const Color(0xFF10B981) : Colors.white,
          borderRadius: BorderRadius.circular(12), // rounded-xl
          border: isSelected ? null : Border.all(color: Colors.grey[200]!),
          boxShadow: isSelected
              ? [
                  BoxShadow(
                    color: const Color(0xFF10B981).withValues(alpha: 0.4),
                    blurRadius: 20,
                    offset: const Offset(0, 0),
                  ),
                ]
              : [
                  BoxShadow(
                    color: Colors.black.withValues(alpha: 0.05),
                    blurRadius: 2,
                    offset: const Offset(0, 1),
                  ),
                ],
        ),
        child: Row(
          mainAxisSize: MainAxisSize.min,
          children: [
            Text(
              label,
              style: GoogleFonts.plusJakartaSans(
                fontSize: 14,
                fontWeight: FontWeight.w600,
                color: isSelected ? Colors.white : Colors.grey[500],
              ),
            ),
            if (icon != null) ...[
              const SizedBox(width: 4),
              Icon(
                icon,
                size: 16,
                color: isSelected ? Colors.white : Colors.grey[500],
              ),
            ],
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionCard(TransactionSummary transaction) {
    final isIncome = transaction.type.toLowerCase() == "income";

    // Map categories to colors similar to MD
    Color bgIconColor;
    Color iconColor;
    IconData icon;

    switch (transaction.categoryName.toLowerCase()) {
      case 'food':
      case 'dining':
        bgIconColor = const Color(0xFFFFEDD5); // orange-100
        iconColor = const Color(0xFFF97316); // orange-500
        icon = Icons.lunch_dining;
        break;
      case 'transport':
      case 'transportation':
        bgIconColor = const Color(0xFFDBEAFE); // blue-100
        iconColor = const Color(0xFF3B82F6); // blue-500
        icon = Icons.local_taxi;
        break;
      case 'salary':
      case 'income':
        bgIconColor = const Color(0xFFDCFCE7); // green-100
        iconColor = const Color(0xFF22C55E); // green-500
        icon = Icons.account_balance;
        break;
      case 'shopping':
        bgIconColor = const Color(0xFFF3E8FF); // purple-100
        iconColor = const Color(0xFFA855F7); // purple-500
        icon = Icons.shopping_bag;
        break;
      case 'entertainment':
      case 'movie':
        bgIconColor = const Color(0xFFFEE2E2); // red-100
        iconColor = const Color(0xFFEF4444); // red-500
        icon = Icons.movie;
        break;
      case 'health':
      case 'medical':
        bgIconColor = const Color(0xFFCCFBF1); // teal-100
        iconColor = const Color(0xFF14B8A6); // teal-500
        icon = Icons.pets;
        break;
      default:
        bgIconColor = const Color(0xFFF3F4F6); // gray-100
        iconColor = const Color(0xFF6B7280); // gray-500
        icon = CategoryIcons.of(transaction.categoryName.toLowerCase());
    }

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16), // rounded-2xl
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04), // shadow-soft
            blurRadius: 40,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: bgIconColor,
              borderRadius: BorderRadius.circular(12), // rounded-xl
            ),
            child: Icon(icon, color: iconColor, size: 24),
          ),
          const SizedBox(width: 16),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Expanded(
                      child: Text(
                        transaction.transactionName,
                        style: GoogleFonts.plusJakartaSans(
                          fontWeight: FontWeight.w700,
                          fontSize: 16,
                          color: const Color(0xFF111827),
                        ),
                        maxLines: 1,
                        overflow: TextOverflow.ellipsis,
                      ),
                    ),
                    const SizedBox(width: 8),
                    Text(
                      '${isIncome ? '+' : '-'} ₹${transaction.amount.abs().toStringAsFixed(2)}',
                      style: GoogleFonts.plusJakartaSans(
                        fontWeight: FontWeight.w700,
                        fontSize: 16,
                        color: isIncome
                            ? const Color(0xFF10B981)
                            : const Color(0xFF111827),
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 4),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text(
                      transaction.categoryName,
                      style: GoogleFonts.plusJakartaSans(
                        color: const Color(0xFF6B7280),
                        fontSize:
                            14, // Slightly larger than typical xs for readability
                        fontWeight: FontWeight.w500,
                      ),
                    ),
                    Text(
                      DateFormat('hh:mm a').format(transaction.occurredAt),
                      style: GoogleFonts.plusJakartaSans(
                        color: const Color(
                          0xFF9CA3AF,
                        ), // text-secondary (lighter)
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
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
}
