import 'package:finance_app/utils/app_style_constants.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';

class AddAccountPage extends StatefulWidget {
  const AddAccountPage({super.key});

  @override
  State<AddAccountPage> createState() => _AddAccountPageState();
}

class _AddAccountPageState extends State<AddAccountPage> with TickerProviderStateMixin {
  final _formKey = GlobalKey<FormState>();
  final _nameController = TextEditingController();
  String _selectedType = 'Bank';
  DateTime _openingDate = DateTime.now();
  final _balanceController = TextEditingController();
  bool _isPositiveBalance = true;
  String _selectedCurrency = 'INR';
  final _creditLimitController = TextEditingController();
  String _cutoffDay = 'Day 1 of month';
  String _dueDay = 'Day 1 of month';
  final _notesController = TextEditingController();
  bool _hideFromSelection = false;
  bool _hideFromReports = false;

  bool _isSubmitting = false;

  final List<String> _accountTypes = ['Bank', 'Cash', 'Credit Card', 'Investment', 'Loan'];
  final List<String> _currencies = ['INR', 'USD', 'EUR', 'GBP'];

  final List<String> _dayOptions = [
    'Day 1 of month', 'Day 2 of month', 'Day 3 of month', 'Day 4 of month',
    'Day 5 of month', 'Day 6 of month', 'Day 7 of month', 'Day 8 of month',
    'Day 9 of month', 'Day 10 of month', 'Day 11 of month', 'Day 12 of month',
    'Day 13 of month', 'Day 14 of month', 'Day 15 of month', 'Day 16 of month',
    'Day 17 of month', 'Day 18 of month', 'Day 19 of month', 'Day 20 of month',
    'Day 21 of month', 'Day 22 of month', 'Day 23 of month', 'Day 24 of month',
    'Day 25 of month', 'Day 26 of month', 'Day 27 of month', 'Day 28 of month',
    'Day 29 of month', 'Day 30 of month', 'Day 31 of month'
  ];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      appBar: AppBar(
        title: Text("Add Account"),
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
            _buildTypeField(),
            const SizedBox(height: 16),
            _buildOpeningDateField(),
            const SizedBox(height: 16),
            _buildBalanceField(),
            const SizedBox(height: 16),
            _buildCurrencyField(),
            // Credit Card specific fields
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
      )
      ),
      // Positioned(
      //       left: 0,
      //       right: 0,
      //       bottom: 0,
      //       child: _buildStickyBottomAction(),
      //     ),
        ])
    );
  }

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
            items: _accountTypes.map((type) => DropdownMenuItem(
              value: type,
              child: Text(type),
            )).toList(),
            onChanged: (value) {
              setState(() {
                _selectedType = value!;
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
                border: Border(bottom: BorderSide(color: Colors.grey[300]!, width: 1)),
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
    return _buildContainer(
      Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            'Starting Balance',
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
                      return 'Please enter balance';
                    }
                    return null;
                  },
                ),
              ),
              const SizedBox(width: 16),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 12),
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
            items: _currencies.map((currency) => DropdownMenuItem(
              value: currency,
              child: Text(currency),
            )).toList(),
            onChanged: (value) {
              setState(() {
                _selectedCurrency = value!;
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
            value: _cutoffDay,
            decoration: const InputDecoration(
              border: InputBorder.none,
              contentPadding: EdgeInsets.zero,
            ),
            items: _dayOptions.map((day) => DropdownMenuItem(
              value: day,
              child: Text(day),
            )).toList(),
            onChanged: (value) {
              setState(() {
                _cutoffDay = value!;
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
            value: _dueDay,
            decoration: const InputDecoration(
              border: InputBorder.none,
              contentPadding: EdgeInsets.zero,
            ),
            items: _dayOptions.map((day) => DropdownMenuItem(
              value: day,
              child: Text(day),
            )).toList(),
            onChanged: (value) {
              setState(() {
                _dueDay = value!;
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
              setState(() {
                _hideFromSelection = value!;
              });
            },
            controlAffinity: ListTileControlAffinity.leading,
            contentPadding: EdgeInsets.zero,
          ),
          CheckboxListTile(
            title: const Text('Hide from account selection and all reports'),
            value: _hideFromReports,
            onChanged: (value) {
              setState(() {
                _hideFromReports = value!;
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

  Future<void> _selectDate() async {
    final DateTime? picked = await showDatePicker(
      context: context,
      initialDate: _openingDate,
      firstDate: DateTime(2000),
      lastDate: DateTime(2030),
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
      // TODO: Save account to API
      await Future.delayed(const Duration(seconds: 1)); // Simulate API call

      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(
            content: Text('Account added successfully!'),
            backgroundColor: Colors.green,
          ),
        );

        Navigator.pop(context);
      }
    } catch (e) {
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('Failed to add account: $e'),
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
            color: Colors.black.withOpacity(0.1),
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
            backgroundColor: _isFormValid() ? AppColors.primaryBlue : AppColors.border,
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
                        valueColor: AlwaysStoppedAnimation<Color>(AppColors.surface),
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
                    Icon(
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
    super.dispose();
  }
}
