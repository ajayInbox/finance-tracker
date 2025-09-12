// lib/pages/transactions_page.dart
import 'package:flutter/material.dart';
import 'add_transaction_page.dart';
import '../data/services/transaction_service.dart';
import '../data/repository/transaction_repository.dart';
import '../data/models/_transaction.dart';

class TransactionsPage extends StatefulWidget {
  const TransactionsPage({super.key});

  @override
  State<TransactionsPage> createState() => _TransactionsPageState();
}

class _TransactionsPageState extends State<TransactionsPage> {
  late Future<List<Transaction>> transactions;

  @override
  void initState() {
    super.initState();
    final repo = HttpTransactionRepository(
      baseUrl: Uri.parse('http://localhost:8080'),
    );
    final svc = TransactionService(repo);
    transactions = svc.getFeed(); // trigger GET on load
  }
  
  // Mock data - replace with actual data from your backend
  // final List<Transaction> transactions = [
  //   Transaction('Swiggy', 'Food & Dining', -450, DateTime.now(), Icons.restaurant),
  //   Transaction('Uber', 'Transport', -250, DateTime.now().subtract(const Duration(hours: 2)), Icons.directions_car),
  //   Transaction('Salary', 'Income', 65000, DateTime.now().subtract(const Duration(days: 1)), Icons.account_balance_wallet),
  //   Transaction('Grocery Store', 'Groceries', -1200, DateTime.now().subtract(const Duration(days: 1)), Icons.shopping_cart),
  //   Transaction('Netflix', 'Subscriptions', -799, DateTime.now().subtract(const Duration(days: 2)), Icons.play_circle_outline),
  // ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: const Text('Transactions'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          IconButton(
            onPressed: () {},
            icon: const Icon(Icons.search),
          ),
          IconButton(
            onPressed: () {},
            icon: const Icon(Icons.filter_list),
          ),
        ],
      ),
      body: FutureBuilder<List<Transaction>>(
        future: transactions,
        builder: (context, snap) {
          if (snap.connectionState != ConnectionState.done) {
            return const Center(child: CircularProgressIndicator());
          }
          if (snap.hasError) {
            return Center(child: Text('Error: ${snap.error}'));
          }
          final items = snap.data!;
          return ListView.builder(
            itemCount: items.length,
            itemBuilder: (_, i) {
              final t = items[i];
              return _buildTransactionCard(t);
              // return ListTile(
              //   title: Text(t.id),
              //   subtitle: Text('${t.occuredAt}'),
              //   trailing: Text('${t.currency} ${t.amount}'),
              // );
            },
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

  Widget _buildTransactionCard(Transaction transaction) {
    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withOpacity(0.1),
            spreadRadius: 1,
            blurRadius: 4,
            offset: const Offset(0, 1),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            padding: const EdgeInsets.all(10),
            decoration: BoxDecoration(
              color: transaction.type.toLowerCase()=="income" ? Colors.green[50] : Colors.red[50],
              borderRadius: BorderRadius.circular(10),
            ),
             child: Text("data")
             //Icon(
            //   transaction.icon,
            //   color: transaction.amount > 0 ? Colors.green[600] : Colors.red[600],
            //   size: 24,
            // ),
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
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  transaction.category,
                  style: TextStyle(
                    color: Colors.grey[600],
                    fontSize: 14,
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  transaction.occuredAt.toString(),
                  style: TextStyle(
                    color: Colors.grey[500],
                    fontSize: 12,
                  ),
                ),
              ],
            ),
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '${transaction.type.toLowerCase()=="income" ? '+' : '-'}₹${transaction.amount.abs()}',
                style: TextStyle(
                  color: transaction.type.toLowerCase()=="income" ? Colors.green[600] : Colors.red[600],
                  fontWeight: FontWeight.bold,
                  fontSize: 16,
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }
}


