import 'package:finance_app/utils/app_style_constants.dart';
import 'package:finance_app/data/models/account_category.dart';
import 'package:finance_app/data/models/account_create_update_request.dart';
import 'package:finance_app/data/services/account_service.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class AddAccountPage extends StatefulWidget {
  final AccountCategory category;

  const AddAccountPage({
    super.key,
    required this.category,
  });

  @override
  State<AddAccountPage> createState() => _AddAccountPageState();
}

class _AddAccountPageState extends State<AddAccountPage>
    with TickerProviderStateMixin {
  final _formKey = GlobalKey<FormState>();

  // Controllers
  final _nameController = TextEditingController();
  final _balanceController = TextEditingController();
  final _creditLimitController = TextEditingController();
  final _notesController = TextEditingController();
  final _lastFourController = TextEditingController();

  // State
  late String _selectedType; // depends on category
  DateTime _openingDate = DateTime.now();
  bool _isPositiveBalance = true;
  String _selectedCurrency = 'INR';
  String _cutoffDay = 'Day 1 of month';
  String _dueDay = 'Day 1 of month';
  bool _hideFromSelection = false;
  bool _hideFromReports = false;
  bool _isSubmitting = false;

  late List<String> _accountTypes; // depends on category

  final List<String> _currencies = ['INR', 'USD', 'EUR', 'GBP'];

  final List<String> _dayOptions = [
    'Day 1 of month',
    'Day 2 of month',
    'Day 3 of month',
    'Day 4 of month',
    'Day 5 of month',
    'Day 6 of month',
    'Day 7 of month',
    'Day 8 of month',
    'Day 9 of month',
    'Day 10 of month',
    'Day 11 of month',
    'Day 12 of month',
    'Day 13 of month',
    'Day 14 of month',
    'Day 15 of month',
    'Day 16 of month',
    'Day 17 of month',
    'Day 18 of month',
    'Day 19 of month',
    'Day 20 of month',
    'Day 21 of month',
    'Day 22 of month',
    'Day 23 of month',
    'Day 24 of month',
    'Day 25 of month',
    'Day 26 of month',
    'Day 27 of month',
    'Day 28 of month',
    'Day 29 of month',
    'Day 30 of month',
    'Day 31 of month',
  ];

  bool get _isLiability => widget.category == AccountCategory.liability;

  @override
  void initState() {
    super.initState();

    // 🔧 Configure based on category
    if (widget.category == AccountCategory.asset) {
      _accountTypes = ['Bank']; // you can later add 'Cash', 'Investment', etc.
      _selectedType = 'Bank';
      _isPositiveBalance = true;
    } else {
      _accountTypes = ['Credit Card']; // liability
      _selectedType = 'Credit Card';
      _isPositiveBalance = false; // usually you owe here
    }
  }

  @override
  Widget build(BuildContext context) {
    final title =
    _isLiability ? 'Add Liability Account' : 'Add Asset Account';

    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text(title),
        backgroundColor: Colors.white,
        foregroundColor: Colors.black,
        elevation: 0,
      ),
      body: Stack(
        children: [
          Form(
            key: _formKey,
            child: ListView(
              padding: const EdgeInsets.all(16),
              children: [
                _buildAccountNameField(),
                const SizedBox(height: 16),
                _buildLastFourDigitField(),
                const SizedBox(height: 16),
                _buildTypeField(),
                const SizedBox(height: 16),
                _buildOpeningDateField(),
                const SizedBox(height: 16),
                _buildBalanceField(),
                const SizedBox(height: 16),
                _buildCurrencyField(),
                // Credit Card specific fields (liability)
                if (_selectedType == 'Credit Card') ...[
                  const SizedBox(height: 16),
                  _buildCreditLimitField(),
                  const SizedBox(height: 16),
                  _buildCutoffDayField(),
                  const SizedBox(height: 16),
                  _buildDueDayField(),
                ],
                const SizedBox(height: 16),
                _buildNotesField(),
                const SizedBox(height: 16),
                _buildHideOptions(),
                const SizedBox(height: 32),
                _buildStickyBottomAction(),
              ],
            ),
          ),
        ],
      ),
    );
  }

  // ---------------- FIELDS ----------------

  Widget _buildAccountNameField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Account Name',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          TextFormField(
            controller: _nameController,
            decoration: const InputDecoration(
              border: InputBorder.none,
              hintText: 'Enter account name',
              hintStyle: TextStyle(color: Colors.grey),
            ),
            validator: (value) {
              if (value == null || value.isEmpty) {
                return 'Please enter account name';
              }
              return null;
            },
          ),
        ],
      ),
    );
  }

  Widget _buildLastFourDigitField(){
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
        Text(
        'Last 4 digits',
        style: TextStyle(
          fontSize: 14,
          fontWeight: FontWeight.w500,
          color: Colors.grey[700],
        ),
      ),
      const SizedBox(height: 8),
      TextFormField(
        controller: _lastFourController,
        maxLength: 4,
        keyboardType: TextInputType.number,
        decoration: const InputDecoration(
          counterText: '',
          hintText: '1234',
          hintStyle: TextStyle(color: Colors.grey),
          border: InputBorder.none,
        ),
        validator: (value) {
          if (value == null || value.isEmpty) {
            return 'Enter last 4 digits';
          }
          if (value.length != 4 || int.tryParse(value) == null) {
            return 'Must be exactly 4 digits';
          }
          return null;
        },
      ),
      ]
      )
    );
  }

  Widget _buildTypeField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Type',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            initialValue: _selectedType,
            decoration: const InputDecoration(
              border: InputBorder.none,
              contentPadding: EdgeInsets.zero,
            ),
            hint: const Text('Select account type'),
            items: _accountTypes
                .map(
                  (type) => DropdownMenuItem(
                value: type,
                child: Text(type),
              ),
            )
                .toList(),
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _selectedType = value;
              });
            },
          ),
        ],
      ),
    );
  }

  Widget _buildOpeningDateField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Opening Date',
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
                border: Border(
                  bottom: BorderSide(
                    color: Colors.grey[300]!,
                    width: 1,
                  ),
                ),
              ),
              child: Row(
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Text(
                    DateFormat('d MMMM yyyy').format(_openingDate),
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

  Widget _buildBalanceField() {
    final label = _isLiability ? 'Current Outstanding' : 'Starting Balance';

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
          Row(
            children: [
              Expanded(
                child: TextFormField(
                  controller: _balanceController,
                  keyboardType: TextInputType.number,
                  decoration: const InputDecoration(
                    border: InputBorder.none,
                    hintText: '0.00',
                    hintStyle: TextStyle(color: Colors.grey),
                  ),
                  validator: (value) {
                    if (value == null || value.isEmpty) {
                      return 'Please enter amount';
                    }
                    return null;
                  },
                ),
              ),
              const SizedBox(width: 16),
              Container(
                padding:
                const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
                decoration: BoxDecoration(
                  color: _isPositiveBalance ? Colors.green : Colors.red,
                  borderRadius: BorderRadius.circular(4),
                ),
                child: InkWell(
                  onTap: () {
                    setState(() {
                      _isPositiveBalance = !_isPositiveBalance;
                    });
                  },
                  child: Icon(
                    _isPositiveBalance ? Icons.add : Icons.remove,
                    color: Colors.white,
                    size: 20,
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildCurrencyField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Default Currency',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            initialValue: _selectedCurrency,
            decoration: const InputDecoration(
              border: InputBorder.none,
              contentPadding: EdgeInsets.zero,
            ),
            items: _currencies
                .map(
                  (currency) => DropdownMenuItem(
                value: currency,
                child: Text(currency),
              ),
            )
                .toList(),
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _selectedCurrency = value;
              });
            },
          ),
        ],
      ),
    );
  }

  Widget _buildCreditLimitField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Credit Limit',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          TextFormField(
            controller: _creditLimitController,
            keyboardType: TextInputType.number,
            decoration: const InputDecoration(
              border: InputBorder.none,
              hintText: 'Enter credit limit',
              hintStyle: TextStyle(color: Colors.grey),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildCutoffDayField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Cutoff Day',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            initialValue: _cutoffDay,
            decoration: const InputDecoration(
              border: InputBorder.none,
              contentPadding: EdgeInsets.zero,
            ),
            items: _dayOptions
                .map(
                  (day) => DropdownMenuItem(
                value: day,
                child: Text(day),
              ),
            )
                .toList(),
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _cutoffDay = value;
              });
            },
          ),
        ],
      ),
    );
  }

  Widget _buildDueDayField() {
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Due Date',
            style: TextStyle(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: Colors.grey[700],
            ),
          ),
          const SizedBox(height: 8),
          DropdownButtonFormField<String>(
            initialValue: _dueDay,
            decoration: const InputDecoration(
              border: InputBorder.none,
              contentPadding: EdgeInsets.zero,
            ),
            items: _dayOptions
                .map(
                  (day) => DropdownMenuItem(
                value: day,
                child: Text(day),
              ),
            )
                .toList(),
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _dueDay = value;
              });
            },
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

  Widget _buildHideOptions() {
    return _buildContainer(
      Column(
        children: [
          CheckboxListTile(
            title: const Text('Hide from account selection'),
            value: _hideFromSelection,
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _hideFromSelection = value;
              });
            },
            controlAffinity: ListTileControlAffinity.leading,
            contentPadding: EdgeInsets.zero,
          ),
          CheckboxListTile(
            title: const Text(
                'Hide from account selection and all reports'),
            value: _hideFromReports,
            onChanged: (value) {
              if (value == null) return;
              setState(() {
                _hideFromReports = value;
              });
            },
            controlAffinity: ListTileControlAffinity.leading,
            contentPadding: EdgeInsets.zero,
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

  // ---------------- ACTIONS ----------------

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _openingDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2035),
    );
    if (picked != null && picked != _openingDate) {
      setState(() {
        _openingDate = picked;
      });
    }
  }

  Future<void> _saveAccount() async {
    if (!_formKey.currentState!.validate()) return;

    setState(() => _isSubmitting = true);

    try {
      // Parse balance
      final balanceAmount = double.tryParse(_balanceController.text) ?? 0.0;
      final signedBalance = _isPositiveBalance ? balanceAmount : -balanceAmount;

      // For assets: startingBalance = signedBalance, currentOutstanding = 0
      // For liabilities: startingBalance = 0, currentOutstanding = signedBalance.abs() (since outstanding is always positive)
      final startingBalance = _isLiability ? 0.0 : signedBalance;
      final currentOutstanding = _isLiability ? signedBalance.abs() : 0.0;

      // Parse credit limit
      final creditLimit = double.tryParse(_creditLimitController.text) ?? 0.0;

      // Create request object
      final request = AccountCreateUpdateRequest(
        accountName: _nameController.text.trim(),
        lastFour: int.parse(_lastFourController.text),
        accountType: _selectedType,
        openingDate: _openingDate,
        startingBalance: startingBalance,
        currentOutstanding: currentOutstanding,
        currency: _selectedCurrency,
        creditLimit: creditLimit,
        cutOffDay: _cutoffDay,
        dueDate: _dueDay,
        notes: _notesController.text.trim(),
        hideFromSelection: _hideFromSelection,
        hideFromReports: _hideFromReports,
        category: _isLiability ? 'liability' : 'asset',
      );

      // Call API
      final accountService = AccountService();
      await accountService.createAccount(request);

      if (!mounted) return;

      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('Account added successfully!'),
          backgroundColor: Colors.green,
        ),
      );

      // Pop with true => so parent knows to refresh
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to add account: $e'),
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      if (mounted) {
        setState(() => _isSubmitting = false);
      }
    }
  }

  Widget _buildStickyBottomAction() {
    return Container(
      padding: const EdgeInsets.all(AppSpacing.space4),
      decoration: BoxDecoration(
        color: AppColors.surface,
        border: Border(
          top: BorderSide(color: AppColors.border),
        ),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.1),
            offset: const Offset(0, -4),
            blurRadius: 8,
          ),
        ],
      ),
      child: SizedBox(
        width: double.infinity,
        height: 52,
        child: ElevatedButton(
          onPressed: _isFormValid() ? _saveAccount : null,
          style: ElevatedButton.styleFrom(
            backgroundColor: _isFormValid()
                ? AppColors.primaryBlue
                : AppColors.border,
            foregroundColor: AppColors.surface,
            elevation: 0,
            shape: RoundedRectangleBorder(
              borderRadius: BorderRadius.circular(AppRadius.radiusFull),
            ),
          ),
          child: _isSubmitting
              ? Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              SizedBox(
                width: 20,
                height: 20,
                child: CircularProgressIndicator(
                  strokeWidth: 2,
                  valueColor: AlwaysStoppedAnimation<Color>(
                    AppColors.surface,
                  ),
                ),
              ),
              const SizedBox(width: AppSpacing.space2),
              Text(
                'Creating account...',
                style: TextStyle(
                  fontSize: AppTypography.textMd,
                  fontWeight: AppTypography.weightSemibold,
                ),
              ),
            ],
          )
              : Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.add,
                size: 20,
              ),
              const SizedBox(width: AppSpacing.space2),
              Text(
                'Add Account',
                style: TextStyle(
                  fontSize: AppTypography.textLg,
                  fontWeight: AppTypography.weightSemibold,
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  bool _isFormValid() {
    return _nameController.text.length >= 3 &&
        _selectedType.isNotEmpty &&
        _balanceController.text.isNotEmpty;
  }

  @override
  void dispose() {
    _nameController.dispose();
    _balanceController.dispose();
    _creditLimitController.dispose();
    _notesController.dispose();
    _lastFourController.dispose();
    super.dispose();
  }
}
