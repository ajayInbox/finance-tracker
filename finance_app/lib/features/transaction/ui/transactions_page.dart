// lib/pages/transactions_page.dart
import 'package:finance_app/features/transaction/application/transaction_controller.dart';
import 'package:finance_app/features/transaction/data/model/transaction_result.dart';
import 'package:finance_app/features/transaction/ui/transaction_form_page.dart';
import 'package:finance_app/utils/category_icon.dart';
import 'package:finance_app/widgets/filter_bottom_sheet.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';

class TransactionsPage extends ConsumerStatefulWidget {
  const TransactionsPage({super.key});

  @override
  ConsumerState<TransactionsPage> createState() => _TransactionsPageState();
}

class _TransactionsPageState extends ConsumerState<TransactionsPage>
    with TickerProviderStateMixin {
  late Future<List<TransactionSummary>> transactions;
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';

  // Filter state
  String _selectedTimeFilter = 'All';
  String _selectedAccountFilter = 'All';
  String _selectedCategoryFilter = 'All';

  // UI state
  bool _isRefreshing = false;
  bool _isSearching = false;
  bool _isMultiSelectMode = false;
  Set<String> _selectedTransactions = {};
  bool _showBalances = false;
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
    await ref
      .read(transactionsControllerProvider.notifier)
      .refresh();
  }

  List<TransactionSummary> _filterTransactions(List<TransactionSummary> transactions) {
    return transactions.where((transaction) {
      // Search filter
      final matchesSearch = _searchQuery.isEmpty ||
          transaction.transactionName.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          transaction.categoryName.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          transaction.amount.toString().contains(_searchQuery);

      // Time filter (simplified for now)
      final matchesTime = _selectedTimeFilter == 'All';

      // Account filter
      final matchesAccount = _selectedAccountFilter == 'All' ||
          transaction.accountName.toLowerCase().contains(_selectedAccountFilter.toLowerCase());

      // Category filter
      final matchesCategory = _selectedCategoryFilter == 'All' ||
          transaction.categoryName.toLowerCase().contains(_selectedCategoryFilter.toLowerCase());

      return matchesSearch && matchesTime && matchesAccount && matchesCategory;
    }).toList();
  }

  void _clearAllFilters() {
    setState(() {
      _selectedTimeFilter = 'All';
      _selectedAccountFilter = 'All';
      _selectedCategoryFilter = 'All';
    });
  }

  void _toggleMultiSelect() {
    setState(() {
      _isMultiSelectMode = !_isMultiSelectMode;
      if (!_isMultiSelectMode) {
        _selectedTransactions.clear();
      }
    });
  }

  void _toggleTransactionSelection(String transactionId) {
    setState(() {
      if (_selectedTransactions.contains(transactionId)) {
        _selectedTransactions.remove(transactionId);
      } else {
        _selectedTransactions.add(transactionId);
      }
    });
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
          onClearAll: _clearAllFilters
        ),
      ),
    );
  }

  @override
Widget build(BuildContext context) {
  final transactionsAsync =
      ref.watch(transactionsControllerProvider);

  return Scaffold(
    backgroundColor: Colors.grey[50],
    appBar: _buildAppBar(),
    body: transactionsAsync.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (e, _) => Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.error_outline, size: 64, color: Colors.red[300]),
                  const SizedBox(height: 16),
                  Text('Error: ${e}', style: TextStyle(color: Colors.red[600])),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _refreshTransactions,
                    child: const Text('Retry'),
                  ),
                ],
              ),
            ),
      data: (items) {
        final filteredItems = _filterTransactions(items);

        if (filteredItems.isEmpty) {
          return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(
                    Icons.receipt_long_outlined,
                    size: 64,
                    color: Colors.grey[400],
                  ),
                  const SizedBox(height: 16),
                  Text(
                    _searchQuery.isNotEmpty
                        ? 'No transactions found for "$_searchQuery"'
                        : (_selectedTimeFilter != 'All' || _selectedAccountFilter != 'All' ||
                           _selectedCategoryFilter != 'All')
                            ? 'No transactions match the selected filters'
                            : 'No transactions yet',
                    style: TextStyle(color: Colors.grey[600], fontSize: 16),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    'Add your first transaction to get started',
                    style: TextStyle(color: Colors.grey[500], fontSize: 14),
                  ),
                ],
              ),
            );
        }

        return RefreshIndicator(
          onRefresh: _refreshTransactions,
          child: ListView.builder(
            controller: _scrollController,
            padding: const EdgeInsets.all(16),
            itemCount: filteredItems.length,
            itemBuilder: (_, i) =>
                _buildTransactionCard(filteredItems[i]),
          ),
        );
      },
    ),
  );
}

  PreferredSizeWidget _buildAppBar(){
    return AppBar(
        title: _searchQuery.isNotEmpty
            ? TextField(
                controller: _searchController,
                decoration: const InputDecoration(
                  hintText: 'Search transactions...',
                  border: InputBorder.none,
                  hintStyle: TextStyle(color: Colors.grey),
                ),
                style: const TextStyle(color: Colors.black, fontSize: 18),
                onChanged: (value) {
                  setState(() {
                    _searchQuery = value;
                  });
                },
              )
            : const Text('Transactions'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          IconButton(
            onPressed: () {
              setState(() {
                if (_searchQuery.isEmpty) {
                  _searchQuery = '';
                  _searchController.clear();
                } else {
                  _searchQuery = '';
                  _searchController.clear();
                }
              });
            },
            icon: Icon(_searchQuery.isNotEmpty ? Icons.close : Icons.search),
          ),
          IconButton(
            onPressed: _showFilterDialog,
            icon: const Icon(Icons.filter_list),
          ),
        ],
      );
  }

  Widget _buildTransactionCard(TransactionSummary transaction) {
    final formattedDate = DateFormat('MMM dd, yyyy').format(transaction.occurredAt);

    return Dismissible(
      key: ValueKey(transaction.id),
      direction: DismissDirection.endToStart,
      background: Container(
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 20),
        decoration: BoxDecoration(
          color: Colors.red,
          borderRadius: BorderRadius.circular(16),
        ),
        child: const Icon(
          Icons.delete,
          color: Colors.white,
        ),
      ),
      confirmDismiss: (direction) async {
        return await _showDeleteConfirmationDialog(context);
      },
      onDismissed: (direction) async {
        try {
          await ref
            .read(transactionsControllerProvider.notifier)
            .deleteTransaction(transaction.id);

          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              const SnackBar(
                content: Text('Transaction deleted successfully'),
                backgroundColor: Colors.green,
              ),
            );
          }
        } catch (e) {

          if (mounted) {
            ScaffoldMessenger.of(context).showSnackBar(
              SnackBar(
                content: Text('Failed to delete transaction: $e'),
                backgroundColor: Colors.red,
              ),
            );
          }
        }
      },
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          color: _selectedTransactions.contains(transaction.id)
              ? Colors.blue.withValues(alpha: 0.1)
              : Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: _selectedTransactions.contains(transaction.id)
              ? Border.all(color: Colors.blue, width: 2)
              : null,
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withValues(alpha: 0.08),
              spreadRadius: 2,
              blurRadius: 8,
              offset: const Offset(0, 2),
            ),
          ],
        ),
        child: Material(
          color: Colors.transparent,
          child: InkWell(
            borderRadius: BorderRadius.circular(16),
            onTap: () => _openTransactionForm(transaction),
            onLongPress: () {
              _toggleMultiSelect();
              _toggleTransactionSelection(transaction.id);
            },
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Row(
                children: [
                  // Category Icon with colored circle
                  Container(
                    width: 48,
                    height: 48,
                    decoration: BoxDecoration(
                      color: transaction.type.toLowerCase() == "income"
                          ? Colors.green.withValues(alpha: 0.1)
                          : Colors.red.withValues(alpha: 0.1),
                      borderRadius: BorderRadius.circular(24),
                    ),
                    child: Icon(
                      CategoryIcons.of(transaction.categoryName.toLowerCase()),
                      color: transaction.type.toLowerCase() == "income"
                          ? Colors.green[600]
                          : Colors.red[600],
                      size: 24,
                    ),
                  ),
                  const SizedBox(width: 16),

                  // Transaction Details
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Line 1: Payee/Category
                        Text(
                          transaction.transactionName,
                          style: const TextStyle(
                            fontWeight: FontWeight.w600,
                            fontSize: 16,
                            color: Colors.black87,
                          ),
                          maxLines: 1,
                          overflow: TextOverflow.ellipsis,
                        ),
                        const SizedBox(height: 4),

                        // Line 2: Date/time - account name - badges
                        Row(
                          children: [
                            Icon(
                              Icons.access_time,
                              size: 14,
                              color: Colors.grey[500],
                            ),
                            const SizedBox(width: 4),
                            Text(
                              formattedDate,
                              style: TextStyle(
                                color: Colors.grey[500],
                                fontSize: 12,
                              ),
                            ),
                            const SizedBox(width: 8),
                            Container(
                              padding: const EdgeInsets.symmetric(horizontal: 6, vertical: 2),
                              decoration: BoxDecoration(
                                color: Colors.grey[100],
                                borderRadius: BorderRadius.circular(4),
                              ),
                              child: Text(
                                transaction.accountName,
                                style: TextStyle(
                                  color: Colors.grey[700],
                                  fontSize: 11,
                                  fontWeight: FontWeight.w500,
                                ),
                              ),
                            ),
                            // Status and metadata badges would go here
                          ],
                        ),
                      ],
                    ),
                  ),

                  // Amount and Balance
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      // Amount with arrow icon
                      Row(
                        children: [
                          Icon(
                            transaction.type.toLowerCase() == "income"
                                ? Icons.arrow_upward
                                : Icons.arrow_downward,
                            size: 16,
                            color: transaction.type.toLowerCase() == "income"
                                ? Colors.green[600]
                                : Colors.red[600],
                          ),
                          const SizedBox(width: 4),
                          Text(
                            '${transaction.type.toLowerCase() == "income" ? '+' : '-'}₹${transaction.amount.abs().toStringAsFixed(2)}',
                            style: TextStyle(
                              color: transaction.type.toLowerCase() == "income"
                                  ? Colors.green[600]
                                  : Colors.red[600],
                              fontWeight: FontWeight.bold,
                              fontSize: 16,
                            ),
                          ),
                        ],
                      ),

                      // Running balance (only if show balances is enabled)
                      if (_showBalances) ...[
                        const SizedBox(height: 4),
                        Text(
                          'Bal: ₹${transaction.balanceCached.toStringAsFixed(2)}',
                          style: TextStyle(
                            color: Colors.grey[500],
                            fontSize: 11,
                          ),
                        ),
                      ],
                    ],
                  ),
                ],
              ),
            ),
          ),
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

  Future<bool?> _showDeleteConfirmationDialog(BuildContext context) {
    return showDialog<bool>(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Delete Transaction'),
          content: const Text('Are you sure you want to delete this transaction?'),
          actions: [
            TextButton(
              onPressed: () => Navigator.of(context).pop(false),
              child: const Text('Cancel'),
            ),
            TextButton(
              onPressed: () => Navigator.of(context).pop(true),
              style: TextButton.styleFrom(foregroundColor: Colors.red),
              child: const Text('Delete'),
            ),
          ],
        );
      },
    );
  }
}
