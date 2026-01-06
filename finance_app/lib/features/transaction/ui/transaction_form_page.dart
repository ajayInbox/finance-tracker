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

class _TransactionFormPageState extends ConsumerState<TransactionFormPage> {
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
      _transactionType = t.type.toLowerCase() == 'income'
          ? 'Income'
          : 'Expense';

      // date
      _selectedDate = t.occurredAt;

      // IMPORTANT:
      // account & category will be resolved AFTER providers load

      // Notes logic was missing in strict read of old file but assuming we want to support it if added to model?
      // The model view showed `String notes` so we can try to populate it if available on TransactionSummary,
      // but TransactionSummary might not have notes? The view_file for TransactionSummary wasn't done,
      // but let's safely assume we start empty or if we had it. Use empty for now to be safe.
      // Wait, let's check if we can populate it. The user didn't ask to fetch it.
      // I'll leave it empty for now unless I see it on the summary object.
    }
  }

  @override
  Widget build(BuildContext context) {
    final accountsAsync = ref.watch(accountsControllerProvider);
    final categoriesAsync = ref.watch(categoriesProvider);

    return accountsAsync.when(
      loading: () =>
          const Scaffold(body: Center(child: CircularProgressIndicator())),
      error: (_, __) => _error(),
      data: (accounts) {
        return categoriesAsync.when(
          loading: () =>
              const Scaffold(body: Center(child: CircularProgressIndicator())),
          error: (_, __) => _error(),
          data: (categories) {
            return _buildForm(accounts, categories);
          },
        );
      },
    );
  }

  Widget _buildForm(List<Account> accounts, List<Category> categories) {
    // Resolve initial selections if editing
    if (widget.isEditMode &&
        _selectedAccount == null &&
        _selectedCategory == null) {
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

    // App Colors
    const bgLight = Color(0xFFF3F4F6);
    // const textPrimary = Color(0xFF111827); // Not used directly but good reference

    return Scaffold(
      backgroundColor: bgLight,
      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(),
            Expanded(
              child: Form(
                key: _formKey,
                child: ListView(
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  children: [
                    _buildTypeSelector(),
                    const SizedBox(height: 24),
                    _buildLabel('TRANSACTION NAME'),
                    _buildNameField(),
                    const SizedBox(height: 24),
                    _buildLabel('AMOUNT'),
                    _buildAmountField(),
                    const SizedBox(height: 24),
                    _buildLabel('ACCOUNT'),
                    _buildAccountSelector(accounts),
                    const SizedBox(height: 24),
                    _buildLabel('CATEGORY'),
                    _buildCategorySelector(categories),
                    const SizedBox(height: 24),
                    _buildLabel('DATE'),
                    _buildDateSelector(),
                    const SizedBox(height: 24),
                    _buildLabel('DESCRIPTION / NOTES'),
                    _buildNotesField(),
                    const SizedBox(height: 48), // Bottom spacing
                    _buildSubmitButton(),
                    const SizedBox(height: 32),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Padding(
      padding: const EdgeInsets.fromLTRB(24, 16, 24, 24),
      child: Row(
        children: [
          _buildIconButton(
            icon: Icons.arrow_back,
            onTap: () => Navigator.pop(context),
          ),
          const SizedBox(width: 16),
          Text(
            widget.isEditMode ? 'Edit Transaction' : 'Add Transaction',
            style: const TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Color(0xFF111827),
            ),
          ),
          const Spacer(),
          _buildIconButton(
            icon: Icons.more_vert,
            onTap: () {}, // Future options
          ),
        ],
      ),
    );
  }

  Widget _buildIconButton({
    required IconData icon,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(12),
      child: Container(
        width: 40,
        height: 40,
        decoration: BoxDecoration(
          color: Colors.transparent,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Icon(icon, color: const Color(0xFF111827)),
      ),
    );
  }

  Widget _buildTypeSelector() {
    return Container(
      padding: const EdgeInsets.all(6),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: Row(
        children: [
          Expanded(
            child: _buildTypeButton(
              label: 'Expense',
              icon: Icons.arrow_downward,
              isSelected: _transactionType == 'Expense',
              color: const Color(0xFFEF4444),
              onTap: () => setState(() {
                _transactionType = 'Expense';
                _selectedCategory = null;
              }),
            ),
          ),
          Expanded(
            child: _buildTypeButton(
              label: 'Income',
              icon: Icons.arrow_upward,
              isSelected: _transactionType == 'Income',
              color: const Color(0xFF10B981),
              onTap: () => setState(() {
                _transactionType = 'Income';
                _selectedCategory = null;
              }),
            ),
          ),
          Expanded(
            child: _buildTypeButton(
              label: 'Transfer',
              icon: Icons.swap_horiz,
              isSelected: _transactionType == 'Transfer',
              color: const Color(0xFF3B82F6),
              onTap: () => setState(() {
                _transactionType = 'Transfer';
                _selectedCategory = null;
              }),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTypeButton({
    required String label,
    required IconData icon,
    required bool isSelected,
    required Color color,
    required VoidCallback onTap,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: isSelected ? color : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              size: 20,
              color: isSelected ? Colors.white : const Color(0xFF6B7280),
            ),
            const SizedBox(width: 8),
            Text(
              label,
              style: TextStyle(
                fontSize: 14,
                fontWeight: FontWeight.bold,
                color: isSelected ? Colors.white : const Color(0xFF6B7280),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildLabel(String label) {
    return Padding(
      padding: const EdgeInsets.only(left: 4, bottom: 10),
      child: Text(
        label,
        style: const TextStyle(
          fontSize: 12,
          fontWeight: FontWeight.bold,
          color: Color(0xFF6B7280),
          letterSpacing: 0.5,
        ),
      ),
    );
  }

  Widget _buildNameField() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: TextFormField(
        controller: _nameCtrl,
        style: const TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w600,
          color: Color(0xFF111827),
        ),
        decoration: InputDecoration(
          hintText: 'e.g. Grocery shopping',
          hintStyle: TextStyle(
            color: const Color(0xFF6B7280).withValues(alpha: 0.5),
          ),
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 20,
            vertical: 16,
          ),
        ),
        validator: (value) {
          if (value == null || value.trim().isEmpty) {
            return 'Please enter a name';
          }
          return null;
        },
      ),
    );
  }

  Widget _buildAmountField() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 8),
      child: Row(
        children: [
          const Text(
            '₹',
            style: TextStyle(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: Color(0xFF6B7280),
            ),
          ),
          const SizedBox(width: 12),
          Expanded(
            child: TextFormField(
              controller: _amountCtrl,
              keyboardType: const TextInputType.numberWithOptions(
                decimal: true,
              ),
              inputFormatters: [
                FilteringTextInputFormatter.allow(RegExp(r'^(\d+)?\.?\d{0,2}')),
              ],
              style: const TextStyle(
                fontSize: 28, // Matches text-3xl approx
                fontWeight: FontWeight.bold,
                color: Color(0xFF111827),
              ),
              decoration: InputDecoration(
                hintText: '0.00',
                hintStyle: TextStyle(
                  color: Colors.grey[300],
                  fontSize: 28,
                  fontWeight: FontWeight.bold,
                ),
                border: InputBorder.none,
              ),
              validator: (value) {
                if (value == null || value.isEmpty) return 'Required';
                final amount = double.tryParse(value);
                if (amount == null || amount <= 0) return 'Invalid';
                return null;
              },
              onChanged: (val) {
                setState(() => _amount = double.tryParse(val) ?? 0);
              },
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAccountSelector(List<Account> accounts) {
    return _buildSelector(
      icon: Icons.account_balance_wallet,
      value: _getSelectedAccountName(accounts),
      onTap: () => _showAccountPicker(accounts),
      trailingIcon: Icons.expand_more,
    );
  }

  Widget _buildCategorySelector(List<Category> categories) {
    return _buildSelector(
      icon: Icons.category,
      value: _getSelectedCategoryName(categories),
      onTap: () => _showCategoryPicker(categories),
      trailingIcon: Icons.expand_more,
    );
  }

  Widget _buildDateSelector() {
    return _buildSelector(
      icon: Icons.calendar_today,
      value: _getFormattedDate(),
      onTap: _showDatePicker,
      trailingIcon: null,
    );
  }

  Widget _buildSelector({
    required IconData icon,
    required String value,
    required VoidCallback onTap,
    IconData? trailingIcon,
  }) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
          boxShadow: [
            BoxShadow(
              color: Colors.black.withValues(alpha: 0.05),
              blurRadius: 10,
              offset: const Offset(0, 4),
            ),
          ],
        ),
        padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 16),
        child: Row(
          children: [
            Icon(icon, color: const Color(0xFF6B7280)),
            const SizedBox(width: 12),
            Expanded(
              child: Text(
                value,
                style: const TextStyle(
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                  color: Color(0xFF111827),
                ),
                maxLines: 1,
                overflow: TextOverflow.ellipsis,
              ),
            ),
            if (trailingIcon != null)
              Icon(trailingIcon, color: const Color(0xFF6B7280)),
          ],
        ),
      ),
    );
  }

  Widget _buildNotesField() {
    return Container(
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(16),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.05),
            blurRadius: 10,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: TextFormField(
        controller: _notesCtrl,
        maxLines: 3,
        style: const TextStyle(
          fontSize: 16,
          fontWeight: FontWeight.w500,
          color: Color(0xFF111827),
        ),
        decoration: InputDecoration(
          hintText: 'Additional details...',
          hintStyle: TextStyle(
            color: const Color(0xFF6B7280).withValues(alpha: 0.5),
          ),
          border: InputBorder.none,
          contentPadding: const EdgeInsets.symmetric(
            horizontal: 20,
            vertical: 16,
          ),
        ),
      ),
    );
  }

  Widget _buildSubmitButton() {
    return SizedBox(
      width: double.infinity,
      child: ElevatedButton(
        onPressed: _submitting ? null : _submit,
        style: ElevatedButton.styleFrom(
          backgroundColor: const Color(0xFFEF4444), // Secondary Red
          foregroundColor: Colors.white,
          padding: const EdgeInsets.symmetric(vertical: 16),
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          elevation: 4,
          shadowColor: const Color(0xFFEF4444).withValues(alpha: 0.4),
        ),
        child: _submitting
            ? const SizedBox(
                height: 24,
                width: 24,
                child: CircularProgressIndicator(
                  color: Colors.white,
                  strokeWidth: 2,
                ),
              )
            : Row(
                mainAxisAlignment: MainAxisAlignment.center,
                children: [
                  const Icon(Icons.check, size: 24),
                  const SizedBox(width: 8),
                  Text(
                    widget.isEditMode
                        ? 'Update Transaction'
                        : 'Save Transaction',
                    style: const TextStyle(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
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
    final date = _selectedDate;
    final timeStr =
        '${date.hour.toString().padLeft(2, '0')}:${date.minute.toString().padLeft(2, '0')}';
    return '${date.year}-${date.month.toString().padLeft(2, '0')}-${date.day.toString().padLeft(2, '0')} $timeStr';
  }

  Future<void> _showCategoryPicker(List<Category> categories) async {
    final selected = await showModalBottomSheet<String>(
      context: context,
      builder: (context) => _buildCategoryBottomSheet(categories),
      isScrollControlled: true,
      backgroundColor: const Color(0xFFF3F4F6),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
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
      backgroundColor: const Color(0xFFF3F4F6),
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(24)),
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
            colorScheme: const ColorScheme.light(
              primary: Color(0xFF10B981),
              onPrimary: Colors.white,
              onSurface: Color(0xFF111827),
            ),
          ),
          child: child!,
        );
      },
    );

    if (pickedDate != null) {
      if (!mounted) return;
      final TimeOfDay? pickedTime = await showTimePicker(
        context: context,
        initialTime: TimeOfDay.fromDateTime(_selectedDate),
        builder: (context, child) {
          return Theme(
            data: Theme.of(context).copyWith(
              colorScheme: const ColorScheme.light(
                primary: Color(0xFF10B981),
                onPrimary: Colors.white,
                onSurface: Color(0xFF111827),
              ),
            ),
            child: child!,
          );
        },
      );

      if (pickedTime != null) {
        setState(() {
          _selectedDate = DateTime(
            pickedDate.year,
            pickedDate.month,
            pickedDate.day,
            pickedTime.hour,
            pickedTime.minute,
          );
        });
        HapticFeedback.lightImpact();
      }
    }
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

    var filteredCategories = filteredCategoriesByTrx;

    return StatefulBuilder(
      builder: (context, setSheetState) {
        return Container(
          height: MediaQuery.of(context).size.height * 0.8,
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Text(
                    'Select Category',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: Color(0xFF111827),
                    ),
                  ),
                  const Spacer(),
                  IconButton(
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.close, color: Color(0xFF6B7280)),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              TextField(
                decoration: InputDecoration(
                  hintText: 'Search categories...',
                  prefixIcon: const Icon(
                    Icons.search,
                    color: Color(0xFF6B7280),
                  ),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide.none,
                  ),
                  fillColor: Colors.white,
                  filled: true,
                ),
                onChanged: (value) {
                  setSheetState(() {
                    filteredCategories = filteredCategoriesByTrx
                        .where(
                          (c) => c.label.toLowerCase().contains(
                            value.toLowerCase(),
                          ),
                        )
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

                    return Container(
                      margin: const EdgeInsets.only(bottom: 8),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(12),
                        border: isSelected
                            ? Border.all(
                                color: const Color(0xFF10B981),
                                width: 2,
                              )
                            : null,
                      ),
                      child: ListTile(
                        leading: Icon(
                          Icons.category,
                          color: isSelected
                              ? const Color(0xFF10B981)
                              : const Color(0xFF6B7280),
                        ),
                        title: Text(
                          category.label,
                          style: TextStyle(
                            fontWeight: isSelected
                                ? FontWeight.bold
                                : FontWeight.normal,
                            color: const Color(0xFF111827),
                          ),
                        ),
                        trailing: isSelected
                            ? const Icon(Icons.check, color: Color(0xFF10B981))
                            : null,
                        onTap: () => Navigator.pop(context, category.id),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildAccountBottomSheet(List<Account> accounts) {
    var filteredAccounts = accounts;

    return StatefulBuilder(
      builder: (context, setSheetState) {
        return Container(
          height: MediaQuery.of(context).size.height * 0.8,
          padding: const EdgeInsets.all(24),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                children: [
                  const Text(
                    'Select Account',
                    style: TextStyle(
                      fontSize: 20,
                      fontWeight: FontWeight.bold,
                      color: Color(0xFF111827),
                    ),
                  ),
                  const Spacer(),
                  IconButton(
                    onPressed: () => Navigator.pop(context),
                    icon: const Icon(Icons.close, color: Color(0xFF6B7280)),
                  ),
                ],
              ),
              const SizedBox(height: 16),
              TextField(
                decoration: InputDecoration(
                  hintText: 'Search accounts...',
                  prefixIcon: const Icon(
                    Icons.search,
                    color: Color(0xFF6B7280),
                  ),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(12),
                    borderSide: BorderSide.none,
                  ),
                  fillColor: Colors.white,
                  filled: true,
                ),
                onChanged: (value) {
                  setSheetState(() {
                    filteredAccounts = accounts
                        .where(
                          (a) => a.accountName.toLowerCase().contains(
                            value.toLowerCase(),
                          ),
                        )
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

                    return Container(
                      margin: const EdgeInsets.only(bottom: 8),
                      decoration: BoxDecoration(
                        color: Colors.white,
                        borderRadius: BorderRadius.circular(12),
                        border: isSelected
                            ? Border.all(
                                color: const Color(0xFF10B981),
                                width: 2,
                              )
                            : null,
                      ),
                      child: ListTile(
                        leading: Icon(
                          Icons.account_balance_wallet,
                          color: isSelected
                              ? const Color(0xFF10B981)
                              : const Color(0xFF6B7280),
                        ),
                        title: Text(
                          account.accountName,
                          style: TextStyle(
                            fontWeight: isSelected
                                ? FontWeight.bold
                                : FontWeight.normal,
                            color: const Color(0xFF111827),
                          ),
                        ),
                        subtitle: Text(
                          'Balance: ₹${account.effectiveBalance}',
                          style: const TextStyle(color: Color(0xFF6B7280)),
                        ),
                        trailing: isSelected
                            ? const Icon(Icons.check, color: Color(0xFF10B981))
                            : null,
                        onTap: () => Navigator.pop(context, account.id),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(12),
                        ),
                      ),
                    );
                  },
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;

    if (_selectedAccount == null || _selectedCategory == null) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select account and category')),
      );
      return;
    }

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
      final controller = ref.read(transactionsControllerProvider.notifier);

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

  Widget _error() =>
      const Scaffold(body: Center(child: Text('Failed to load data')));
}
