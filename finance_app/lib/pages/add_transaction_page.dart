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
import 'package:telephony/telephony.dart';
import 'package:permission_handler/permission_handler.dart';

class AddTransactionPage extends StatefulWidget {
  const AddTransactionPage({super.key, this.prefillParsed});

  final ParsedTransaction? prefillParsed;

  @override
  State<AddTransactionPage> createState() => _AddTransactionPageState();
}

class _AddTransactionPageState extends State<AddTransactionPage> {
  final _formKey = GlobalKey<FormState>();
  final _amountController = TextEditingController();
  final _notesController = TextEditingController();
  late List<Account> accounts = [];
  late List<Category> categories = [];

  bool _isSubmitting = false;

  String _transactionType = 'Expense';
  String? _selectedCategory;
  String? _selectedAccount;
  DateTime _selectedDate = DateTime.now();
  double _setAmount = 0.0;


  @override
  void initState(){
    super.initState();
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
      backgroundColor: Colors.grey[50],
      appBar: AppBar(
        title: const Text('Add Transaction'),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
        actions: [
          TextButton(
            onPressed: _scanSms,
            child: const Text(
              'Scan SMS',
              style: TextStyle(
                color: Colors.blue,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
          TextButton(
            onPressed: _isSubmitting ? null : _submitTransaction,
            child: Text(
              _isSubmitting ? 'Saving...' : 'Save',
              style: TextStyle(
                color: _isSubmitting ? Colors.grey : Colors.blue,
                fontWeight: FontWeight.w600,
              ),
            ),
          ),
        ],
      ),
      body: Form(
        key: _formKey,
        child: ListView(
          padding: const EdgeInsets.all(16),
          children: [
            _buildTransactionTypeSelector(),
            const SizedBox(height: 24),
            _buildAmountField(),
            const SizedBox(height: 16),
            _buildCategoryField(),
            const SizedBox(height: 16),
            _buildAccountField(),
            const SizedBox(height: 16),
            _buildDateField(),
            const SizedBox(height: 16),
            _buildNotesField(),
            const SizedBox(height: 32),
            _buildSubmitButton(),
          ],
        ),
      ),
    );
  }

  Widget _buildTransactionTypeSelector() {
    return Container(
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
          Expanded(
            child: _buildTypeOption('Expense', Colors.red),
          ),
          Expanded(
            child: _buildTypeOption('Income', Colors.green),
          ),
        ],
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
            style: const TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
            decoration: const InputDecoration(
              prefixText: '₹ ',
              prefixStyle: TextStyle(fontSize: 24, fontWeight: FontWeight.bold),
              border: InputBorder.none,
              hintText: '0.00',
              hintStyle: TextStyle(color: Colors.grey),
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
        ],
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

  @override
  void dispose() {
    _amountController.dispose();
    _notesController.dispose();
    super.dispose();
  }
}
