// lib/pages/add_transaction_page.dart

import 'package:finance_app/data/models/account.dart';
import 'package:finance_app/data/models/category.dart';
import 'package:finance_app/data/models/transaction.dart';
import 'package:finance_app/data/services/account_service.dart';
import 'package:finance_app/data/services/category_service.dart';
import 'package:finance_app/data/services/transaction_service.dart';
import 'package:finance_app/utils/message_parser.dart';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:another_telephony/telephony.dart';
import 'package:permission_handler/permission_handler.dart';

class AddTransactionPage extends StatefulWidget {
  const AddTransactionPage({super.key, this.prefillParsed});

  final ParsedTransaction? prefillParsed;

  @override
  State<AddTransactionPage> createState() => _AddTransactionPageState();
}

class _AddTransactionPageState extends State<AddTransactionPage>
    with TickerProviderStateMixin {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _notesController = TextEditingController();
  late List<Account> accounts = [];
  late List<Category> categories = [];
  late List<Category> _filteredCategories = [];
  late List<Account> _filteredAccounts = [];

  bool _isSubmitting = false;
  bool _showAdvanced = false;
  bool _isRecurring = false;
  bool _isSplit = false;
  bool _isTransfer = false;

  String _transactionType = 'Expense';
  String? _selectedCategory;
  String? _selectedAccount;
  DateTime _selectedDate = DateTime.now();
  double _setAmount = 0.0;

  // Smart defaults tracking
  String? _lastUsedCategory;
  String? _lastUsedAccount;

  // SMS parsing state
  ParsedTransaction? _parsedTransaction;
  bool _showSmsBanner = false;

  // Animation controllers
  late AnimationController _amountAnimationController;
  late AnimationController _saveAnimationController;

  @override
  void initState(){
    super.initState();
    _amountAnimationController = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _saveAnimationController = AnimationController(
      duration: const Duration(milliseconds: 200),
      vsync: this,
    );
    _loadData();
  }

  Future<void> _loadData() async {
    try {
      final accountsData = await AccountService().getAccounts();
      final categoriesData = await CategoryService().getAllCategories();
      if (mounted) {
        setState(() {
          accounts = accountsData;
          categories = categoriesData;
          if (widget.prefillParsed != null) {
            _prefillFromParsed(widget.prefillParsed!);
          }
        });
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Failed to load accounts and categories'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _prefillFromParsed(ParsedTransaction parsed) {
    _setAmount = parsed.amount ?? 0.0;
    _amountController.text = parsed.amount?.toString() ?? '';
    _notesController.text = 'Auto-filled from SMS';  // Or full message, but not available

    if (parsed.categoryHint != null) {
      final category = categories.firstWhere(
        (c) => c.label == parsed.categoryHint,
        orElse: () => categories.first,
      );
      _selectedCategory = category.id;
    }
    // Assume first account and today's date
    if (accounts.isNotEmpty) {
      _selectedAccount = accounts.first.id;
    }
    _selectedDate = parsed.date ?? DateTime.now();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).colorScheme.surface,
      appBar: AppBar(
        title: const Text('Add Transaction'),
        backgroundColor: Colors.transparent,
        foregroundColor: Theme.of(context).colorScheme.onSurface,
        elevation: 0,
        actions: [
          if (_parsedTransaction != null)
            Container(
              margin: const EdgeInsets.only(right: 8),
              padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.primaryContainer,
                borderRadius: BorderRadius.circular(12),
              ),
              child: Text(
                'SMS Parsed',
                style: TextStyle(
                  fontSize: 12,
                  color: Theme.of(context).colorScheme.onPrimaryContainer,
                ),
              ),
            ),
          IconButton(
            onPressed: _scanSms,
            icon: const Icon(Icons.sms),
            tooltip: 'Scan SMS',
          ),
        ],
      ),
      body: Stack(
        children: [
          Form(
            key: _formKey,
            child: ListView(
              padding: const EdgeInsets.fromLTRB(16, 16, 16, 100), // Bottom padding for sticky button
              children: [
                // SMS Banner
                if (_showSmsBanner && _parsedTransaction != null)
                  _buildSmsBanner(),

                // Transaction Type Selector
                _buildTransactionTypeSelector(),
                const SizedBox(height: 16),

                // Large Amount Field with Quick Chips
                _buildAmountField(),
                const SizedBox(height: 16),

                // Essentials Stack (compact cards)
                _buildEssentialsStack(),
                const SizedBox(height: 16),

                // Notes Field
                _buildNotesField(),
                const SizedBox(height: 16),

                // Advanced Section
                if (_showAdvanced) ...[
                  _buildAdvancedSection(),
                  const SizedBox(height: 16),
                ],

                // Expand Advanced Button
                _buildExpandAdvancedButton(),
              ],
            ),
          ),

          // Sticky Bottom Action Bar
          _buildStickyBottomBar(),
        ],
      ),
    );
  }

  Widget _buildSmsBanner() {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.primaryContainer.withOpacity(0.1),
        borderRadius: BorderRadius.circular(12),
        border: Border.all(
          color: Theme.of(context).colorScheme.primary,
          width: 1,
        ),
      ),
      child: Row(
        children: [
          Icon(
            Icons.sms,
            color: Theme.of(context).colorScheme.primary,
            size: 20,
          ),
          const SizedBox(width: 8),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Parsed from SMS',
                  style: TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                    color: Theme.of(context).colorScheme.primary,
                  ),
                ),
                Text(
                  'Confidence: 82% • Tap to edit',
                  style: TextStyle(
                    fontSize: 12,
                    color: Theme.of(context).colorScheme.onSurfaceVariant,
                  ),
                ),
              ],
            ),
          ),
          TextButton(
            onPressed: () => setState(() => _showSmsBanner = false),
            child: Text(
              'Edit',
              style: TextStyle(
                color: Theme.of(context).colorScheme.primary,
                fontSize: 12,
              ),
            ),
          ),
        ],
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
              Icon(Icons.remove, size: 16, color: _transactionType == 'Expense' ? Colors.red : Colors.grey),
              const SizedBox(width: 4),
              const Text('Expense'),
            ],
          ),
          enabled: !_isSubmitting,
        ),
        ButtonSegment(
          value: 'Income',
          label: Row(
            mainAxisSize: MainAxisSize.min,
            children: [
              Icon(Icons.add, size: 16, color: _transactionType == 'Income' ? Colors.green : Colors.grey),
              const SizedBox(width: 4),
              const Text('Income'),
            ],
          ),
          enabled: !_isSubmitting,
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
            return _transactionType == 'Expense'
                ? Colors.red.withOpacity(0.1)
                : Colors.green.withOpacity(0.1);
          }
          return Colors.transparent;
        }),
        foregroundColor: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return _transactionType == 'Expense' ? Colors.red : Colors.green;
          }
          return Colors.grey[600];
        }),
        side: WidgetStateProperty.resolveWith((states) {
          if (states.contains(WidgetState.selected)) {
            return BorderSide(
              color: _transactionType == 'Expense' ? Colors.red : Colors.green,
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

  Widget _buildTypeOption(String type, Color color) {
    final isSelected = _transactionType == type;
    
    return GestureDetector(
      onTap: () {
        setState(() {
          _transactionType = type;
          _selectedCategory = null; // Reset category when type changes
        });
      },
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 16),
        decoration: BoxDecoration(
          color: isSelected ? color.withOpacity(0.1) : Colors.transparent,
          borderRadius: BorderRadius.circular(12),
          border: isSelected ? Border.all(color: color, width: 2) : null,
        ),
        child: Text(
          type,
          textAlign: TextAlign.center,
          style: TextStyle(
            color: isSelected ? color : Colors.grey[600],
            fontWeight: isSelected ? FontWeight.w600 : FontWeight.normal,
            fontSize: 16,
          ),
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
            controller: _amountController,
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
              setState(() => _setAmount = amount);
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

  Widget _buildQuickAmountChip(double amount) {
    return InkWell(
      onTap: () {
        setState(() {
          _setAmount = amount;
          _amountController.text = amount.toString();
        });
        HapticFeedback.lightImpact();
      },
      borderRadius: BorderRadius.circular(16),
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.primaryContainer,
          borderRadius: BorderRadius.circular(16),
          border: Border.all(
            color: Theme.of(context).colorScheme.primary.withOpacity(0.3),
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

  Widget _buildEssentialsStack() {
    return Column(
      children: [
        // Category and Account in one row
        Row(
          children: [
            Expanded(
              child: _buildCompactCard(
                title: 'Category',
                value: _getSelectedCategoryName(),
                icon: Icons.category,
                onTap: () => _showCategoryPicker(),
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildCompactCard(
                title: 'Account',
                value: _getSelectedAccountName(),
                icon: Icons.account_balance_wallet,
                onTap: () => _showAccountPicker(),
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        // Date in full width
        _buildCompactCard(
          title: 'Date',
          value: _getFormattedDate(),
          icon: Icons.calendar_today,
          onTap: () => _showDatePicker(),
          isFullWidth: true,
        ),
      ],
    );
  }

  Widget _buildCompactCard({
    required String title,
    required String value,
    required IconData icon,
    required VoidCallback onTap,
    bool isFullWidth = false,
  }) {
    return InkWell(
      onTap: onTap,
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

  String _getSelectedCategoryName() {
    if (_selectedCategory == null) return 'Select Category';
    final category = categories.firstWhere(
      (c) => c.id == _selectedCategory,
      orElse: () => categories.first,
    );
    return category.label;
  }

  String _getSelectedAccountName() {
    if (_selectedAccount == null) return 'Select Account';
    final account = accounts.firstWhere(
      (a) => a.id == _selectedAccount,
      orElse: () => accounts.first,
    );
    return account.accountName;
  }

  String _getFormattedDate() {
    return '${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}';
  }

  Future<void> _showCategoryPicker() async {
    final selected = await showModalBottomSheet<String>(
      context: context,
      builder: (context) => _buildCategoryBottomSheet(),
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

  Future<void> _showAccountPicker() async {
    final selected = await showModalBottomSheet<String>(
      context: context,
      builder: (context) => _buildAccountBottomSheet(),
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
    final DateTime? picked = await showDatePicker(
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

    if (picked != null && picked != _selectedDate) {
      setState(() => _selectedDate = picked);
      HapticFeedback.lightImpact();
    }
  }

  Widget _buildContainer(Widget child) {
    return Container(
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
      child: child,
    );
  }

  Widget _buildCategoryField() {
    return _buildDropdownField<Category>(
      'Category',
      _selectedCategory,
      categories,
      (category) => category.label,
      (value) => setState(() => _selectedCategory = value),
    );
  }

  Widget _buildAccountField() {
    return _buildDropdownField<Account>(
      'Account',
      _selectedAccount,
      accounts,
      (account) => account.accountName,
      (value) => setState(() => _selectedAccount = value),
    );
  }

  Widget _buildDropdownField<T>(
    String label,
    String? value,
    List<T> items,
    String Function(T) getDisplayText,
    Function(String?) onChanged,
  ) {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            initialValue: value,
            decoration: const InputDecoration(
              border: InputBorder.none,
              contentPadding: EdgeInsets.zero,
            ),
            hint: Text('Select $label'),
            items: items.map((item) => DropdownMenuItem(
              value: _getItemId(item),
              child: Text(getDisplayText(item)),
            )).toList(),
            onChanged: onChanged,
            validator: (value) {
              if (value == null) {
                return 'Please select a $label';
              }
              return null;
            },
          ),
        ],
      ),
    );
  }

  String _getItemId(dynamic item) {
    if (item is Account) return item.id;
    if (item is Category) return item.id;
    throw ArgumentError('Unsupported item type');
  }

  Widget _buildDateField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Date',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          GestureDetector(
            onTap: _selectDate,
            child: Container(
              padding: const EdgeInsets.symmetric(vertical: 12),
              decoration: BoxDecoration(
                border: Border(bottom: BorderSide(color: Colors.grey[300]!, width: 1)),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    '${_selectedDate.day}/${_selectedDate.month}/${_selectedDate.year}',
                    style: const TextStyle(fontSize: 16),
                  ),
                  const Icon(Icons.calendar_today, color: Colors.grey),
                ],
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildNotesField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Notes (Optional)',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          TextFormField(
            controller: _notesController,
            maxLines: 3,
            decoration: const InputDecoration(
              border: InputBorder.none,
              hintText: 'Add a note...',
              hintStyle: TextStyle(color: Colors.grey),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSubmitButton() {
    return SizedBox(
      width: double.infinity,
      height: 50,
      child: ElevatedButton(
        onPressed: _isSubmitting ? null : _submitTransaction,
        style: ElevatedButton.styleFrom(
          backgroundColor: _isSubmitting ? Colors.grey : Colors.blue,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(12),
          ),
          elevation: 0,
        ),
        child: Text(
          _isSubmitting ? 'Adding Transaction...' : 'Add Transaction',
          style: const TextStyle(
            fontSize: 16,
            fontWeight: FontWeight.w600,
            color: Colors.white,
          ),
        ),
      ),
    );
  }

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _selectedDate,
      firstDate: DateTime(2020),
      lastDate: DateTime.now(),
    );
    if (picked != null && picked != _selectedDate) {
      setState(() {
        _selectedDate = picked;
      });
    }
  }

  Future<void> _submitTransaction() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSubmitting = true);

    try {
      // Create transaction object from form data
      final transaction = Transaction(
        transactionName: '${_transactionType} Transaction',
        amount: _setAmount,
        type: _transactionType,
        account: _selectedAccount!,
        category: _selectedCategory!,
        occuredAt: _selectedDate,
        notes: _notesController.text.trim().isEmpty ? '' : _notesController.text.trim(),
      );

      // Call API to add transaction
      await TransactionService().addTransaction(transaction);

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Transaction added successfully!'),
            backgroundColor: Colors.green,
          ),
        );

        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to add transaction: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  Future<void> _scanSms() async {
    final status = await Permission.sms.status;
    if (!status.isGranted) {
      final newStatus = await Permission.sms.request();
      if (!newStatus.isGranted) {
        if (mounted) {
          ScaffoldMessenger.of(context).showSnackBar(
            const SnackBar(
              content: Text('SMS permission is required to scan messages'),
              backgroundColor: Colors.red,
            ),
          );
        }
        return;
      }
    }

    try {
      final telephony = Telephony.instance;
      final messages = await telephony.getInboxSms(
        filter: SmsFilter.where(SmsColumn.BODY).like('%'),
        sortOrder: [OrderBy(SmsColumn.DATE)],
      );

      // Filter to recent messages, say last 30 days
      final thirtyDaysAgo = DateTime.now().subtract(const Duration(days: 30));
      final recentMessages = messages.where(
        (msg) => msg.date != null && DateTime.fromMillisecondsSinceEpoch(msg.date!).isAfter(thirtyDaysAgo),
      ).toList();

      final selectedMessage = await showDialog<SmsMessage>(
        context: context,
        builder: (context) => AlertDialog(
          title: const Text('Select SMS Message'),
          content: SizedBox(
            width: double.maxFinite,
            height: 300,
            child: recentMessages.isEmpty
                ? const Center(child: Text('No recent SMS messages found'))
                : ListView.builder(
                    itemCount: recentMessages.length,
                    itemBuilder: (context, index) {
                      final msg = recentMessages[index];
                      return ListTile(
                        title: Text(
                          msg.address ?? 'Unknown Sender',
                          style: const TextStyle(fontWeight: FontWeight.bold),
                        ),
                        subtitle: Text(
                          msg.body ?? '',
                          maxLines: 2,
                          overflow: TextOverflow.ellipsis,
                        ),
                        onTap: () => Navigator.pop(context, msg),
                      );
                    },
                  ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: const Text('Cancel'),
            ),
          ],
        ),
      );

      if (selectedMessage != null) {
        _processSms(selectedMessage.body ?? '');
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to read SMS: $e'),
            backgroundColor: Colors.red,
          ),
        );
      }
    }
  }

  void _processSms(String messageBody) {
    final parsed = MessageParser().parse(messageBody);
    if (parsed.isValid) {
      setState(() {
        _setAmount = parsed.amount ?? 0.0;
        _amountController.text = parsed.amount?.toString() ?? '';
        _notesController.text = messageBody;

        // Set category if hint found
        if (parsed.categoryHint != null) {
          final category = categories.firstWhere(
            (c) => c.label == parsed.categoryHint,
            orElse: () => categories.first,
          );
          _selectedCategory = category.id;
        }
      });
    } else {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Could not extract transaction details from this message'),
            backgroundColor: Colors.orange,
          ),
        );
      }
    }
  }

  Widget _buildCategoryBottomSheet() {
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
                _filteredCategories = categories
                    .where((c) => c.label.toLowerCase().contains(value.toLowerCase()))
                    .toList();
              });
            },
          ),
          const SizedBox(height: 16),
          Expanded(
            child: ListView.builder(
              itemCount: categories.length,
              itemBuilder: (context, index) {
                final category = categories[index];
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

  Widget _buildAccountBottomSheet() {
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
                _filteredAccounts = accounts
                    .where((a) => a.accountName.toLowerCase().contains(value.toLowerCase()))
                    .toList();
              });
            },
          ),
          const SizedBox(height: 16),
          Expanded(
            child: ListView.builder(
              itemCount: accounts.length,
              itemBuilder: (context, index) {
                final account = accounts[index];
                final isSelected = _selectedAccount == account.id;

                return ListTile(
                  leading: Icon(Icons.account_balance_wallet, color: isSelected ? Theme.of(context).colorScheme.primary : Colors.grey),
                  title: Text(account.accountName),
                  subtitle: Text('Balance: ₹${account.balance?.toStringAsFixed(2) ?? '0.00'}'),
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

  Widget _buildAdvancedSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Advanced Options',
          style: Theme.of(context).textTheme.titleMedium?.copyWith(
            color: Theme.of(context).colorScheme.primary,
          ),
        ),
        const SizedBox(height: 12),
        _buildAdvancedOption(
          title: 'Recurring Transaction',
          subtitle: 'Set up automatic transactions',
          icon: Icons.repeat,
          value: _isRecurring,
          onChanged: (value) => setState(() => _isRecurring = value),
        ),
        const SizedBox(height: 8),
        _buildAdvancedOption(
          title: 'Split Across Categories',
          subtitle: 'Divide amount between multiple categories',
          icon: Icons.call_split,
          value: _isSplit,
          onChanged: (value) => setState(() => _isSplit = value),
        ),
        const SizedBox(height: 8),
        _buildAdvancedOption(
          title: 'Transfer Mode',
          subtitle: 'Move money between accounts',
          icon: Icons.swap_horiz,
          value: _isTransfer,
          onChanged: (value) => setState(() => _isTransfer = value),
        ),
      ],
    );
  }

  Widget _buildAdvancedOption({
    required String title,
    required String subtitle,
    required IconData icon,
    required bool value,
    required Function(bool) onChanged,
  }) {
    return Container(
      padding: const EdgeInsets.all(12),
      decoration: BoxDecoration(
        color: Colors.grey[50],
        borderRadius: BorderRadius.circular(8),
        border: Border.all(color: Colors.grey[200]!),
      ),
      child: Row(
        children: [
          Icon(icon, color: Theme.of(context).colorScheme.primary),
          const SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  title,
                  style: const TextStyle(
                    fontSize: 14,
                    fontWeight: FontWeight.w600,
                  ),
                ),
                Text(
                  subtitle,
                  style: TextStyle(
                    fontSize: 12,
                    color: Colors.grey[600],
                  ),
                ),
              ],
            ),
          ),
          Switch(
            value: value,
            onChanged: onChanged,
            activeColor: Theme.of(context).colorScheme.primary,
          ),
        ],
      ),
    );
  }

  Widget _buildExpandAdvancedButton() {
    return InkWell(
      onTap: () => setState(() => _showAdvanced = !_showAdvanced),
      borderRadius: BorderRadius.circular(8),
      child: Container(
        padding: const EdgeInsets.symmetric(vertical: 12),
        decoration: BoxDecoration(
          color: Colors.grey[50],
          borderRadius: BorderRadius.circular(8),
          border: Border.all(color: Colors.grey[200]!),
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Text(
              _showAdvanced ? 'Hide Advanced' : 'Show Advanced',
              style: TextStyle(
                color: Theme.of(context).colorScheme.primary,
                fontWeight: FontWeight.w600,
              ),
            ),
            const SizedBox(width: 8),
            Icon(
              _showAdvanced ? Icons.expand_less : Icons.expand_more,
              color: Theme.of(context).colorScheme.primary,
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildStickyBottomBar() {
    return Positioned(
      bottom: 0,
      left: 0,
      right: 0,
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.surface,
          border: Border(
            top: BorderSide(color: Colors.grey[200]!, width: 1),
          ),
        ),
        child: Row(
          children: [
            Expanded(
              child: ElevatedButton.icon(
                onPressed: _canSubmit() ? _submitTransaction : null,
                icon: Icon(
                  _transactionType == 'Expense' ? Icons.remove : Icons.add,
                  size: 20,
                ),
                label: Text(
                  'Save ${_transactionType} ${_setAmount > 0 ? '₹${_setAmount.toStringAsFixed(2)}' : ''}',
                ),
                style: ElevatedButton.styleFrom(
                  backgroundColor: _transactionType == 'Expense' ? Colors.red : Colors.green,
                  foregroundColor: Colors.white,
                  padding: const EdgeInsets.symmetric(vertical: 16),
                  shape: RoundedRectangleBorder(
                    borderRadius: BorderRadius.circular(12),
                  ),
                  elevation: 0,
                ),
              ),
            ),
            const SizedBox(width: 12),
            OutlinedButton.icon(
              onPressed: _canSubmit() ? _submitAndNew : null,
              icon: const Icon(Icons.add, size: 20),
              label: const Text('Save & New'),
              style: OutlinedButton.styleFrom(
                padding: const EdgeInsets.symmetric(vertical: 16, horizontal: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
                side: BorderSide(color: Theme.of(context).colorScheme.primary),
              ),
            ),
          ],
        ),
      ),
    );
  }

  bool _canSubmit() {
    return !_isSubmitting && _setAmount > 0 && _selectedCategory != null && _selectedAccount != null;
  }

  Future<void> _submitAndNew() async {
    await _submitTransaction();
    if (mounted) {
      // Reset form for new transaction
      setState(() {
        _amountController.clear();
        _notesController.clear();
        _setAmount = 0.0;
        _selectedCategory = null;
        _selectedAccount = null;
        _selectedDate = DateTime.now();
        _transactionType = 'Expense';
      });
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _notesController.dispose();
    _amountAnimationController.dispose();
    _saveAnimationController.dispose();
    super.dispose();
  }
}
