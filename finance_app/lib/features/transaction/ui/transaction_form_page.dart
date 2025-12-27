// ui/transaction_form_page.dart
import 'package:finance_app/features/account/application/accounts_controller.dart';
import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/category/data/models/category.dart';
import 'package:finance_app/features/category/providers/categories_provider.dart';
import 'package:finance_app/features/transaction/application/transaction_controller.dart';
import 'package:finance_app/features/transaction/data/model/transaction.dart';
import 'package:finance_app/features/transaction/data/model/transaction_result.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

class TransactionFormPage extends ConsumerStatefulWidget {
  const TransactionFormPage({super.key, this.transaction});

  final TransactionSummary? transaction;
  bool get isEditMode => transaction != null;

  @override
  ConsumerState<TransactionFormPage> createState() =>
      _TransactionFormPageState();
}

class _TransactionFormPageState
    extends ConsumerState<TransactionFormPage> {

  final _formKey = GlobalKey<FormState>();
  final _amountCtrl = TextEditingController();
  final _nameCtrl = TextEditingController();
  final _notesCtrl = TextEditingController();

  String _transactionType = 'Expense';
  String? _selectedAccount;
  String? _selectedCategory;
  DateTime _selectedDate = DateTime.now();
  double _amount = 0;
  bool _submitting = false;

  @override
  void initState() {
    super.initState();

    if (widget.isEditMode) {
      final t = widget.transaction!;

      // amount
      _amount = t.amount.abs();
      _amountCtrl.text = _amount.toStringAsFixed(2);

      // name
      _nameCtrl.text = t.transactionName;

      // transaction type
      _transactionType =
          t.type.toLowerCase() == 'income' ? 'Income' : 'Expense';

      // date
      _selectedDate = t.occurredAt;

      // IMPORTANT:
      // account & category will be resolved AFTER providers load
    }
  }


  @override
  Widget build(BuildContext context) {
    final accountsAsync = ref.watch(accountsControllerProvider);
    final categoriesAsync = ref.watch(categoriesProvider);

    return accountsAsync.when(
      loading: () => const Scaffold(
        body: Center(child: CircularProgressIndicator()),
      ),
      error: (_, __) => _error(),
      data: (accounts) {
        return categoriesAsync.when(
          loading: () => const Scaffold(
            body: Center(child: CircularProgressIndicator()),
          ),
          error: (_, __) => _error(),
          data: (categories) {
            return _buildForm(accounts, categories);
          },
        );
      },
    );
  }

  Widget _buildForm(List<Account> accounts, List<Category> categories) {

    if (widget.isEditMode && _selectedAccount == null && _selectedCategory == null) {
      final t = widget.transaction!;

      final account = accounts.firstWhere(
        (a) => a.accountName == t.accountName,
        orElse: () => accounts.first,
      );

      final category = categories.firstWhere(
        (c) => c.label == t.categoryName,
        orElse: () => categories.first,
      );

      _selectedAccount = account.id;
      _selectedCategory = category.id;
    }

    return Scaffold(
      appBar: AppBar(
        title: Text(widget.isEditMode ? 'Update Transaction' : 'Add Transaction'),
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _buildTransactionTypeSelector(),
            const SizedBox(height: 12),
            _buildAmountField(),
            const SizedBox(height: 12),
            _buildTransactionNameField(),
            const SizedBox(height: 12),
            _buildEssentialsStack(accounts, categories),
            const SizedBox(height: 24),
            ElevatedButton(
              onPressed: _submitting ? null : _submit,
              child: Text(widget.isEditMode ? 'Update' : 'Save'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionTypeSelector() {
    return SegmentedButton<String>(
      segments: [
        ButtonSegment(
          value: 'Expense',
          label: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.remove, size: 16, color: _transactionType.toLowerCase() == 'expense' ? Colors.red : Colors.grey),
              const SizedBox(width: 4),
              const Text('Expense'),
            ],
          ),
          enabled: !_submitting,
        ),
        ButtonSegment(
          value: 'Income',
          label: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.add, size: 16, color: _transactionType.toLowerCase() == 'income' ? Colors.green : Colors.grey),
              const SizedBox(width: 4),
              const Text('Income'),
            ],
          ),
          enabled: !_submitting,
        ),
      ],
      selected: {_transactionType},
      onSelectionChanged: (Set<String> selection) {
        setState(() {
          _transactionType = selection.first;
          _selectedCategory = null; // Reset category when type changes
        });
      },
      style: ButtonStyle(
        backgroundColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return _transactionType.toLowerCase() == 'expense'
                ? Colors.red.withValues(alpha: 0.1)
                : Colors.green.withValues(alpha: 0.1);
          }
          return Colors.transparent;
        }),
        foregroundColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return _transactionType.toLowerCase() == 'expense' ? Colors.red : Colors.green;
          }
          return Colors.grey[600];
        }),
        side: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return BorderSide(
              color: _transactionType.toLowerCase() == 'expense' ? Colors.red : Colors.green,
              width: 1,
            );
          }
          return BorderSide(color: Colors.grey[300]!, width: 1);
        }),
        shape: WidgetStateProperty.all(
          RoundedRectangleBorder(borderRadius: BorderRadius.circular(8)),
        ),
      ),
    );
  }

  Widget _buildAmountField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Amount',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          TextFormField(
            controller: _amountCtrl,
            keyboardType: const TextInputType.numberWithOptions(decimal: true),
            inputFormatters: [
              FilteringTextInputFormatter.allow(RegExp(r'^(\d+)?\.?\d{0,2}')),
            ],
            style: const TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
            decoration: const InputDecoration(
              prefixText: '₹ ',
              prefixStyle: TextStyle(fontSize: 32, fontWeight: FontWeight.bold),
              border: InputBorder.none,
              hintText: '0.00',
              hintStyle: TextStyle(color: Colors.grey, fontSize: 32),
            ),
            validator: (value) {
              if (value == null || value.isEmpty) {
                return 'Please enter an amount';
              }
              final amount = double.tryParse(value);
              if (amount == null || amount <= 0) {
                return 'Please enter a valid amount';
              }
              return null;
            },
            onChanged: (text) {
              final amount = double.tryParse(text) ?? 0.0;
              setState(() => _amount = amount);
            },
          ),
          const SizedBox(height: 12),
          // Quick Amount Chips
          Wrap(
            spacing: 8,
            runSpacing: 8,
            children: [
              _buildQuickAmountChip(100),
              _buildQuickAmountChip(500),
              _buildQuickAmountChip(1000),
              _buildQuickAmountChip(2000),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildTransactionNameField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Transaction Name',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          TextFormField(
            controller: _nameCtrl,
            decoration: const InputDecoration(
              border: InputBorder.none,
              hintText: 'Enter transaction name...',
              hintStyle: TextStyle(color: Colors.grey),
            ),
            validator: (value) {
              if (value == null || value.trim().isEmpty) {
                return 'Please enter a transaction name';
              }
              return null;
            },
          ),
        ],
      ),
    );
  }

  Widget _buildEssentialsStack(
  List<Account> accounts,
  List<Category> categories,
) {
  return Column(
    children: [
      Row(
        children: [
          Expanded(
            child: _buildCompactCard<List<Category>>(
              title: 'Category',
              value: _getSelectedCategoryName(categories),
              icon: Icons.category,
              payload: categories,
              onTap: _showCategoryPicker,
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: _buildCompactCard<List<Account>>(
              title: 'Account',
              value: _getSelectedAccountName(accounts),
              icon: Icons.account_balance_wallet,
              payload: accounts,
              onTap: _showAccountPicker,
            ),
          ),
        ],
      ),
      const SizedBox(height: 12),
      _buildCompactCard<void>(
        title: 'Date',
        value: _getFormattedDate(),
        icon: Icons.calendar_today,
        payload: null,
        onTap: (_) => _showDatePicker(),
        isFullWidth: true,
      ),
    ],
  );
}


  Widget _buildCompactCard<T>({
  required String title,
  required String value,
  required IconData icon,
  required T payload,
  required ValueChanged<T> onTap,
  bool isFullWidth = false,
}) {
  return InkWell(
    onTap: () => onTap(payload),
    borderRadius: BorderRadius.circular(12),
    child: Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        border: Border.all(color: Colors.grey[200]!, width: 1),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Icon(
                icon,
                size: 16,
                color: Theme.of(context).colorScheme.primary,
              ),
              const SizedBox(width: 6),
              Text(
                title,
                style: TextStyle(
                  fontSize: 12,
                  fontWeight: FontWeight.w500,
                  color: Colors.grey[600],
                ),
              ),
            ],
          ),
          const SizedBox(height: 4),
          Text(
            value,
            style: const TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: Colors.black87,
            ),
            maxLines: 1,
            overflow: TextOverflow.ellipsis,
          ),
        ],
      ),
    ),
  );
}


  String _getSelectedCategoryName(List<Category> categories) {
    if (_selectedCategory == null) return 'Select Category';
    final category = categories.firstWhere(
      (c) => c.id == _selectedCategory,
      orElse: () => categories.first,
    );
    return category.label;
  }

  String _getSelectedAccountName(List<Account> accounts) {
    if (_selectedAccount == null) return 'Select Account';
    final account = accounts.firstWhere(
      (a) => a.id == _selectedAccount,
      orElse: () => accounts.first,
    );
    return account.accountName;
  }

  String _getFormattedDate() {
    // Format as ISO 8601 with timezone (Asia/Calcutta is UTC+5:30)
    final timeFormat = _selectedDate.hour > 12
        ? '${_selectedDate.hour - 12}:${_selectedDate.minute.toString().padLeft(2, '0')} PM'
        : '${_selectedDate.hour}:${_selectedDate.minute.toString().padLeft(2, '0')} AM';
    return '${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year} $timeFormat';

  }

  Future<void> _showCategoryPicker(List<Category> categories) async {
    final selected = await showModalBottomSheet<String>(
      context: context,
      builder: (context) => _buildCategoryBottomSheet(categories),
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
    );

    if (selected != null) {
      setState(() => _selectedCategory = selected);
      HapticFeedback.lightImpact();
    }
  }


  Future<void> _showAccountPicker(List<Account> accounts) async {
    final selected = await showModalBottomSheet<String>(
      context: context,
      builder: (context) => _buildAccountBottomSheet(accounts),
      isScrollControlled: true,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
    );

    if (selected != null) {
      setState(() => _selectedAccount = selected);
      HapticFeedback.lightImpact();
    }
  }


  Future<void> _showDatePicker() async {
    final DateTime? pickedDate = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
      builder: (context, child) {
        return Theme(
          data: Theme.of(context).copyWith(
            colorScheme: ColorScheme.light(
              primary: Theme.of(context).colorScheme.primary,
              onPrimary: Colors.white,
              onSurface: Colors.black,
            ),
          ),
          child: child!,
        );
      },
    );

    if (pickedDate != null) {
      final TimeOfDay? pickedTime = await showTimePicker(
        context: context,
        initialTime: TimeOfDay.fromDateTime(_selectedDate),
        builder: (context, child) {
          return Theme(
            data: Theme.of(context).copyWith(
              colorScheme: ColorScheme.light(
                primary: Theme.of(context).colorScheme.primary,
                onPrimary: Colors.white,
                onSurface: Colors.black,
              ),
            ),
            child: child!,
          );
        },
      );

      if (pickedTime != null) {
        final DateTime combinedDateTime = DateTime(
          pickedDate.year,
          pickedDate.month,
          pickedDate.day,
          pickedTime.hour,
          pickedTime.minute,
        );
        setState(() => _selectedDate = combinedDateTime);
        HapticFeedback.lightImpact();
      }
    }
  }

  Widget _buildQuickAmountChip(double amount) {
    return InkWell(
      onTap: () {
        setState(() {
          _amount = amount;
          _amountCtrl.text = amount.toString();
        });
      },
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.primaryContainer,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.3),
            width: 1,
          ),
        ),
        child: Text(
          '₹${amount.toInt()}',
          style: TextStyle(
            fontSize: 14,
            fontWeight: FontWeight.w600,
            color: Theme.of(context).colorScheme.primary,
          ),
        ),
      ),
    );
  }

  Widget _buildContainer(Widget child) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(12),
        boxShadow: [
          BoxShadow(
            color: Colors.grey.withValues(alpha: 0.1),
            spreadRadius: 1,
            blurRadius: 4,
            offset: const Offset(0, 1),
          ),
        ],
      ),
      child: child,
    );
  }

  Widget _buildCategoryBottomSheet(List<Category> categories) {
    // Filter categories based on transaction type
    final filteredCategoriesByTrx = categories.where((c) {
      if (_transactionType == 'Expense') {
        return c.isExpense();
      } else if (_transactionType == 'Income') {
        return c.isIncome();
      }
      return true; // Show all if neither
    }).toList();

    // Initialize _filteredCategories with all filtered categories
    var filteredCategories = filteredCategoriesByTrx;

    return Container(
      height: MediaQuery.of(context).size.height * 0.8,
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                'Select Category',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const Spacer(),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.close),
              ),
            ],
          ),
          const SizedBox(height: 16),
          TextField(
            decoration: InputDecoration(
              hintText: 'Search categories...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            onChanged: (value) {
              setState(() {
                filteredCategories = filteredCategories
                    .where((c) => c.label.toLowerCase().contains(value.toLowerCase()))
                    .toList();
              });
            },
          ),
          const SizedBox(height: 16),
          Expanded(
            child: ListView.builder(
              itemCount: filteredCategories.length,
              itemBuilder: (context, index) {
                final category = filteredCategories[index];
                final isSelected = _selectedCategory == category.id;

                return ListTile(
                  leading: Icon(Icons.category, color: isSelected ? Theme.of(context).colorScheme.primary : Colors.grey),
                  title: Text(category.label),
                  trailing: isSelected ? Icon(Icons.check, color: Theme.of(context).colorScheme.primary) : null,
                  onTap: () => Navigator.pop(context, category.id),
                );
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAccountBottomSheet(List<Account> accounts) {
    // Initialize _filteredAccounts with all accounts
    var filteredAccounts = accounts;

    return Container(
      height: MediaQuery.of(context).size.height * 0.8,
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Text(
                'Select Account',
                style: Theme.of(context).textTheme.titleLarge,
              ),
              const Spacer(),
              IconButton(
                onPressed: () => Navigator.pop(context),
                icon: const Icon(Icons.close),
              ),
            ],
          ),
          const SizedBox(height: 16),
          TextField(
            decoration: InputDecoration(
              hintText: 'Search accounts...',
              prefixIcon: const Icon(Icons.search),
              border: OutlineInputBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            onChanged: (value) {
              setState(() {
                filteredAccounts = accounts
                    .where((a) => a.accountName.toLowerCase().contains(value.toLowerCase()))
                    .toList();
              });
            },
          ),
          const SizedBox(height: 16),
          Expanded(
            child: ListView.builder(
              itemCount: filteredAccounts.length,
              itemBuilder: (context, index) {
                final account = filteredAccounts[index];
                final isSelected = _selectedAccount == account.id;

                return ListTile(
                  leading: Icon(Icons.account_balance_wallet, color: isSelected ? Theme.of(context).colorScheme.primary : Colors.grey),
                  title: Text(account.accountName),
                  subtitle: Text('Balance: ₹${account.effectiveBalance}'),
                  trailing: isSelected ? Icon(Icons.check, color: Theme.of(context).colorScheme.primary) : null,
                  onTap: () => Navigator.pop(context, account.id),
                );
              },
            ),
          ),
        ],
      ),
    );
  }


  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _submitting = true);

    final tx = Transaction(
      transactionName: _nameCtrl.text,
      amount: _amount,
      type: _transactionType,
      account: _selectedAccount!,
      category: _selectedCategory!,
      occurredAt: _selectedDate,
      notes: _notesCtrl.text,
    );

    try {
      final controller =
          ref.read(transactionsControllerProvider.notifier);

      if (widget.isEditMode) {
        await controller.updateTransaction(widget.transaction!.id, tx);
      } else {
        await controller.createTransaction(tx);
      }

      if (mounted) {
        Navigator.pop(context, TransactionResult.success);
      }
    } catch (e, stack) {
      debugPrint('Transaction submit failed: $e');
      debugPrintStack(stackTrace: stack);

      if (mounted) {
        Navigator.pop(context, TransactionResult.failure);
      }
    } finally {
      if (mounted) {
        setState(() => _submitting = false);
      }
    }
  }



  Widget _error() => const Scaffold(
        body: Center(child: Text('Failed to load data')),
      );
}
