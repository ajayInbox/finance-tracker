// lib/pages/transactions_page.dart
import 'package:finance_app/utils/category_icon.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'add_transaction_page.dart';
import '../data/services/transaction_service.dart';
import '../data/models/transaction_summary.dart';

class TransactionsPage extends StatefulWidget {
  const TransactionsPage({super.key});

  @override
  State<TransactionsPage> createState() => _TransactionsPageState();
}

class _TransactionsPageState extends State<TransactionsPage>
    with TickerProviderStateMixin {
  late Future<List<TransactionSummary>> transactions;
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';

  // Filter state
  String _selectedTimeFilter = 'All';
  String _selectedAccountFilter = 'All';
  String _selectedCategoryFilter = 'All';
  String _selectedStatusFilter = 'All';
  double _minAmount = 0;
  double _maxAmount = 10000;

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
    final svc = TransactionService();
    transactions = svc.getFeed();

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
    setState(() {
      _isRefreshing = true;
    });
    final svc = TransactionService();
    transactions = svc.getFeed();
    await transactions;
    setState(() {
      _isRefreshing = false;
    });
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

      // Status filter (simplified for now)
      final matchesStatus = _selectedStatusFilter == 'All';

      // Amount range filter
      final matchesAmount = transaction.amount >= _minAmount && transaction.amount <= _maxAmount;

      return matchesSearch && matchesTime && matchesAccount && matchesCategory && matchesStatus && matchesAmount;
    }).toList();
  }

  void _clearAllFilters() {
    setState(() {
      _selectedTimeFilter = 'All';
      _selectedAccountFilter = 'All';
      _selectedCategoryFilter = 'All';
      _selectedStatusFilter = 'All';
      _minAmount = 0;
      _maxAmount = 10000;
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
          selectedStatusFilter: _selectedStatusFilter,
          minAmount: _minAmount,
          maxAmount: _maxAmount,
          onFiltersChanged: (time, account, category, status, min, max) {
            setState(() {
              _selectedTimeFilter = time;
              _selectedAccountFilter = account;
              _selectedCategoryFilter = category;
              _selectedStatusFilter = status;
              _minAmount = min;
              _maxAmount = max;
            });
          },
          onClearAll: _clearAllFilters,
        ),
      ),
    );
  }


  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
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
      ),
      body: FutureBuilder<List<TransactionSummary>>(
        future: transactions,
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError) {
            return Center(
              child: Column(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  Icon(Icons.error_outline, size: 64, color: Colors.red[300]),
                  const SizedBox(height: 16),
                  Text('Error: ${snap.error}', style: TextStyle(color: Colors.red[600])),
                  const SizedBox(height: 16),
                  ElevatedButton(
                    onPressed: _refreshTransactions,
                    child: const Text('Retry'),
                  ),
                ],
              ),
            );
          }
          final items = snap.data!;
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
                           _selectedCategoryFilter != 'All' || _selectedStatusFilter != 'All')
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
              padding: const EdgeInsets.all(16),
              itemCount: filteredItems.length,
              itemBuilder: (_, i) {
                final t = filteredItems[i];
                return _buildTransactionCard(t);
              },
            ),
          );
        },
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: () {
          Navigator.push(
            context,
            MaterialPageRoute(builder: (context) => const AddTransactionPage()),
          );
        },
        backgroundColor: Colors.blue,
        child: const Icon(Icons.add, color: Colors.white),
      ),
    );
  }

  Widget _buildTransactionCard(TransactionSummary transaction) {
    final formattedDate = DateFormat('MMM dd, yyyy').format(transaction.occuredAt);
    final formattedTime = DateFormat('hh:mm a').format(transaction.occuredAt);

    return Dismissible(
      key: Key(transaction.id),
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
      onDismissed: (direction) {
        // TODO: Delete transaction
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: const Text('Transaction deleted'),
            action: SnackBarAction(
              label: 'Undo',
              onPressed: () {
                // TODO: Undo delete
              },
            ),
          ),
        );
      },
      child: Container(
        margin: const EdgeInsets.only(bottom: 12),
        decoration: BoxDecoration(
          color: _selectedTransactions.contains(transaction.id)
              ? Colors.blue.withOpacity(0.1)
              : Colors.white,
          borderRadius: BorderRadius.circular(16),
          border: _selectedTransactions.contains(transaction.id)
              ? Border.all(color: Colors.blue, width: 2)
              : null,
          boxShadow: [
            BoxShadow(
              color: Colors.grey.withOpacity(0.08),
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
            onTap: () {
              if (_isMultiSelectMode) {
                _toggleTransactionSelection(transaction.id);
              } else {
                // TODO: Navigate to transaction details
              }
            },
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
                          ? Colors.green.withOpacity(0.1)
                          : Colors.red.withOpacity(0.1),
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
                              '$formattedDate at $formattedTime',
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

// Filter Bottom Sheet Widget
class FilterBottomSheet extends StatefulWidget {
  final String selectedTimeFilter;
  final String selectedAccountFilter;
  final String selectedCategoryFilter;
  final String selectedStatusFilter;
  final double minAmount;
  final double maxAmount;
  final Function(String, String, String, String, double, double) onFiltersChanged;
  final VoidCallback onClearAll;

  const FilterBottomSheet({
    super.key,
    required this.selectedTimeFilter,
    required this.selectedAccountFilter,
    required this.selectedCategoryFilter,
    required this.selectedStatusFilter,
    required this.minAmount,
    required this.maxAmount,
    required this.onFiltersChanged,
    required this.onClearAll,
  });

  @override
  State<FilterBottomSheet> createState() => _FilterBottomSheetState();
}

class _FilterBottomSheetState extends State<FilterBottomSheet> {
  late String _timeFilter;
  late String _accountFilter;
  late String _categoryFilter;
  late String _statusFilter;
  late double _minAmount;
  late double _maxAmount;

  @override
  void initState() {
    super.initState();
    _timeFilter = widget.selectedTimeFilter;
    _accountFilter = widget.selectedAccountFilter;
    _categoryFilter = widget.selectedCategoryFilter;
    _statusFilter = widget.selectedStatusFilter;
    _minAmount = widget.minAmount;
    _maxAmount = widget.maxAmount;
  }

  void _applyFilters() {
    widget.onFiltersChanged(_timeFilter, _accountFilter, _categoryFilter, _statusFilter, _minAmount, _maxAmount);
    Navigator.of(context).pop();
  }

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              const Text(
                'Filters',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              TextButton(
                onPressed: widget.onClearAll,
                child: const Text('Clear all'),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Time Period Filter
          const Text(
            'Time Period',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              'All',
              'Today',
              'Yesterday',
              'This Week',
              'This Month',
              'Custom Range'
            ].map((filter) {
              final isSelected = _timeFilter == filter;
              return FilterChip(
                label: Text(filter),
                selected: isSelected,
                onSelected: (selected) {
                  setState(() {
                    _timeFilter = filter;
                  });
                },
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Account Filter
          const Text(
            'Account',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              'All',
              'Main Account',
              'Savings',
              'Credit Card'
            ].map((filter) {
              final isSelected = _accountFilter == filter;
              return FilterChip(
                label: Text(filter),
                selected: isSelected,
                onSelected: (selected) {
                  setState(() {
                    _accountFilter = filter;
                  });
                },
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Category Filter
          const Text(
            'Category',
            style: TextStyle(
              fontSize: 16,
              fontWeight: FontWeight.w600,
            ),
          ),
          const SizedBox(height: 8),
          Wrap(
            spacing: 8,
            children: [
              'All',
              'Food',
              'Transport',
              'Shopping',
              'Entertainment'
            ].map((filter) {
              final isSelected = _categoryFilter == filter;
              return FilterChip(
                label: Text(filter),
                selected: isSelected,
                onSelected: (selected) {
                  setState(() {
                    _categoryFilter = filter;
                  });
                },
              );
            }).toList(),
          ),
          const SizedBox(height: 16),

          // Amount Range
          Row(
            children: [
              const Text(
                'Amount Range',
                style: TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
              const Spacer(),
              Text(
                '₹${_minAmount.toStringAsFixed(0)} - ₹${_maxAmount.toStringAsFixed(0)}',
                style: const TextStyle(
                  fontSize: 14,
                  color: Colors.grey,
                ),
              ),
            ],
          ),
          RangeSlider(
            values: RangeValues(_minAmount, _maxAmount),
            min: 0,
            max: 10000,
            divisions: 100,
            labels: RangeLabels(
              '₹${_minAmount.toStringAsFixed(0)}',
              '₹${_maxAmount.toStringAsFixed(0)}',
            ),
            onChanged: (values) {
              setState(() {
                _minAmount = values.start;
                _maxAmount = values.end;
              });
            },
          ),
          const SizedBox(height: 24),

          // Apply Button
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: _applyFilters,
              style: ElevatedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
              child: const Text('Apply Filters'),
            ),
          ),
        ],
      ),
    );
  }
}

// Add Transaction Bottom Sheet Widget
class AddTransactionBottomSheet extends StatelessWidget {
  const AddTransactionBottomSheet({super.key});

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.all(16),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Header
          Row(
            children: [
              const Text(
                'Add Transaction',
                style: TextStyle(
                  fontSize: 20,
                  fontWeight: FontWeight.bold,
                ),
              ),
              const Spacer(),
              IconButton(
                onPressed: () => Navigator.of(context).pop(),
                icon: const Icon(Icons.close),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Quick Actions
          Row(
            children: [
              Expanded(
                child: _buildQuickActionButton(
                  context,
                  'Add Expense',
                  Icons.remove_circle_outline,
                  Colors.red,
                  () {
                    Navigator.of(context).pop();
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const AddTransactionPage(),
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(width: 16),
              Expanded(
                child: _buildQuickActionButton(
                  context,
                  'Add Income',
                  Icons.add_circle_outline,
                  Colors.green,
                  () {
                    Navigator.of(context).pop();
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (context) => const AddTransactionPage(),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),

          // Transfer Button
          SizedBox(
            width: double.infinity,
            child: OutlinedButton.icon(
              onPressed: () {
                Navigator.of(context).pop();
                Navigator.push(
                  context,
                  MaterialPageRoute(
                    builder: (context) => const AddTransactionPage(),
                  ),
                );
              },
              icon: const Icon(Icons.swap_horiz),
              label: const Text('Transfer'),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildQuickActionButton(
    BuildContext context,
    String title,
    IconData icon,
    Color color,
    VoidCallback onPressed,
  ) {
    return ElevatedButton(
      onPressed: onPressed,
      style: ElevatedButton.styleFrom(
        backgroundColor: color.withOpacity(0.1),
        foregroundColor: color,
        elevation: 0,
        padding: const EdgeInsets.symmetric(vertical: 20),
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(12),
        ),
      ),
      child: Column(
        children: [
          Icon(icon, size: 32),
          const SizedBox(height: 8),
          Text(
            title,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
            ),
          ),
        ],
      ),
    );
  }
}
