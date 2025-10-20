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

class _TransactionsPageState extends State<TransactionsPage> {
  late Future<List<TransactionSummary>> transactions;
  final TextEditingController _searchController = TextEditingController();
  String _searchQuery = '';
  String _selectedFilter = 'All';
  bool _isRefreshing = false;

  @override
  void initState() {
    super.initState();
    final svc = TransactionService();
    transactions = svc.getFeed(); // trigger GET on load
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
      final matchesSearch = _searchQuery.isEmpty ||
          transaction.transactionName.toLowerCase().contains(_searchQuery.toLowerCase()) ||
          transaction.categoryName.toLowerCase().contains(_searchQuery.toLowerCase());

      final matchesFilter = _selectedFilter == 'All' ||
          (transaction.type.toLowerCase() == _selectedFilter.toLowerCase());

      return matchesSearch && matchesFilter;
    }).toList();
  }

  void _showFilterDialog() {
    showDialog(
      context: context,
      builder: (BuildContext context) {
        return AlertDialog(
          title: const Text('Filter Transactions'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                title: const Text('All'),
                leading: Radio<String>(
                  value: 'All',
                  groupValue: _selectedFilter,
                  onChanged: (value) {
                    setState(() {
                      _selectedFilter = value!;
                    });
                    Navigator.of(context).pop();
                  },
                ),
              ),
              ListTile(
                title: const Text('Income'),
                leading: Radio<String>(
                  value: 'Income',
                  groupValue: _selectedFilter,
                  onChanged: (value) {
                    setState(() {
                      _selectedFilter = value!;
                    });
                    Navigator.of(context).pop();
                  },
                ),
              ),
              ListTile(
                title: const Text('Expense'),
                leading: Radio<String>(
                  value: 'Expense',
                  groupValue: _selectedFilter,
                  onChanged: (value) {
                    setState(() {
                      _selectedFilter = value!;
                    });
                    Navigator.of(context).pop();
                  },
                ),
              ),
            ],
          ),
        );
      },
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
                        : _selectedFilter != 'All'
                            ? 'No ${_selectedFilter.toLowerCase()} transactions found'
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

  // Widget _buildFilterTabs() {
  //   final filters = ['All', 'Income', 'Expenses', 'This Month'];
    
  //   return Container(
  //     height: 50,
  //     color: Colors.white,
  //     child: ListView.builder(
  //       scrollDirection: Axis.horizontal,
  //       padding: const EdgeInsets.symmetric(horizontal: 16),
  //       itemCount: filters.length,
  //       itemBuilder: (context, index) {
  //         final filter = filters[index];
  //         final isSelected = selectedFilter == filter;
          
  //         return Padding(
  //           padding: const EdgeInsets.only(right: 8),
  //           child: FilterChip(
  //             label: Text(filter),
  //             selected: isSelected,
  //             onSelected: (selected) {
  //               setState(() {
  //                 selectedFilter = filter;
  //               });
  //             },
  //             backgroundColor: Colors.grey[100],
  //             selectedColor: Colors.blue[100],
  //             labelStyle: TextStyle(
  //               color: isSelected ? Colors.blue[800] : Colors.grey[700],
  //               fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
  //             ),
  //           ),
  //         );
  //       },
  //     ),
  //   );
  // }

  // Widget _buildTransactionsList() {
  //   return ListView.builder(
  //     padding: const EdgeInsets.all(16),
  //     itemCount: transactions.length,
  //     itemBuilder: (context, index) {
  //       final transaction = transactions[index];
  //       return _buildTransactionCard(transaction);
  //     },
  //   );
  // }

  Widget _buildSummaryHeader(List<TransactionSummary> transactions) {
    final totalIncome = transactions
        .where((t) => t.type.toLowerCase() == 'income')
        .fold(0.0, (sum, t) => sum + t.amount);

    final totalExpenses = transactions
        .where((t) => t.type.toLowerCase() == 'expense')
        .fold(0.0, (sum, t) => sum + t.amount.abs());

    final netBalance = totalIncome - totalExpenses;

    return Container(
      margin: const EdgeInsets.all(16),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Colors.blue[600]!,
            Colors.blue[400]!,
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.blue.withOpacity(0.2),
            spreadRadius: 2,
            blurRadius: 8,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Column(
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Total Balance',
                    style: TextStyle(
                      color: Colors.white.withOpacity(0.9),
                      fontSize: 14,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    '₹${netBalance.toStringAsFixed(2)}',
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 24,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
              Icon(
                netBalance >= 0 ? Icons.trending_up : Icons.trending_down,
                color: Colors.white,
                size: 32,
              ),
            ],
          ),
          const SizedBox(height: 16),
          Row(
            children: [
              Expanded(
                child: Column(
                  children: [
                    Text(
                      'Income',
                      style: TextStyle(
                        color: Colors.white.withOpacity(0.8),
                        fontSize: 12,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '₹${totalIncome.toStringAsFixed(2)}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                width: 1,
                height: 24,
                color: Colors.white.withOpacity(0.3),
              ),
              Expanded(
                child: Column(
                  children: [
                    Text(
                      'Expenses',
                      style: TextStyle(
                        color: Colors.white.withOpacity(0.8),
                        fontSize: 12,
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      '₹${totalExpenses.toStringAsFixed(2)}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 16,
                        fontWeight: FontWeight.w600,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTransactionCard(TransactionSummary transaction) {
    final formattedDate = DateFormat('MMM dd, yyyy').format(transaction.occuredAt);
    final formattedTime = DateFormat('hh:mm a').format(transaction.occuredAt);

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
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
            // TODO: Navigate to transaction details
          },
          child: Padding(
            padding: const EdgeInsets.all(16),
            child: Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(12),
                  decoration: BoxDecoration(
                    color: transaction.type.toLowerCase() == "income"
                        ? Colors.green[50]
                        : Colors.red[50],
                    borderRadius: BorderRadius.circular(12),
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
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
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
                      const SizedBox(height: 6),
                      Text(
                        transaction.categoryName,
                        style: TextStyle(
                          color: Colors.grey[600],
                          fontSize: 14,
                        ),
                      ),
                      const SizedBox(height: 4),
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
                        ],
                      ),
                    ],
                  ),
                ),
                const SizedBox(width: 16),
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '${transaction.type.toLowerCase() == "income" ? '+' : '-'}₹${transaction.amount.abs().toStringAsFixed(2)}',
                      style: TextStyle(
                        color: transaction.type.toLowerCase() == "income"
                            ? Colors.green[600]
                            : Colors.red[600],
                        fontWeight: FontWeight.bold,
                        fontSize: 18,
                      ),
                    ),
                    const SizedBox(height: 6),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                      decoration: BoxDecoration(
                        color: Colors.grey[100],
                        borderRadius: BorderRadius.circular(8),
                      ),
                      child: Text(
                        transaction.accountName,
                        style: TextStyle(
                          color: Colors.grey[700],
                          fontWeight: FontWeight.w500,
                          fontSize: 12,
                        ),
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Bal: ₹${transaction.balanceCached.toStringAsFixed(2)}',
                      style: TextStyle(
                        color: Colors.grey[500],
                        fontSize: 11,
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
