import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/account/data/model/account_type.dart';
import 'package:finance_app/features/account/provider/create_account_provider.dart';
import 'package:finance_app/features/account/application/accounts_controller.dart';
import 'package:finance_app/utils/app_style_constants.dart';
import 'package:finance_app/features/account/data/model/account_category.dart';
import 'package:finance_app/features/account/data/model/account_create_update_request.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:flutter/material.dart';

import 'package:google_fonts/google_fonts.dart';

class AddAccountPage extends ConsumerStatefulWidget {
  final AccountCategory category;
  final Account? account;

  const AddAccountPage({super.key, required this.category, this.account});

  @override
  ConsumerState<AddAccountPage> createState() => _AddAccountPageState();
}

class _AddAccountPageState extends ConsumerState<AddAccountPage> {
  final _formKey = GlobalKey<FormState>();

  // Controllers
  final _amountController =
      TextEditingController(); // Unified for Balance/Outstanding
  final _nameController = TextEditingController();
  final _bankNameController = TextEditingController(); // New field for UI
  final _lastFourController = TextEditingController();
  final _creditLimitController = TextEditingController();

  // State
  late String _selectedType;
  final DateTime _openingDate =
      DateTime.now(); // For Asset accounts implicitly today
  String? _statementDay; // For Credit Card
  String? _dueDay; // For Credit Card
  bool _isSubmitting = false;

  // Defaults
  final String _selectedCurrency = 'INR';

  bool get _isLiability => widget.category == AccountCategory.liability;

  @override
  void initState() {
    super.initState();
    if (widget.account != null) {
      final acc = widget.account!;
      _nameController.text = acc.accountName;
      _lastFourController.text = acc.lastFour ?? '';
      _selectedType = acc.accountType.apiValue;
      print(_selectedType);

      // Handle Balance/Outstanding
      if (_isLiability) {
        _amountController.text = (acc.currentOutstanding ?? 0).toString();
        _creditLimitController.text = (acc.creditLimit ?? 0).toString();
        // Handle dates extraction from logic if possible, or default
        if (acc.dueDayOfMonth != null) {
          _dueDay = acc.dueDayOfMonth;
        }
        if (acc.statementDayOfMonth != null) {
          _statementDay = acc.statementDayOfMonth;
        }
      } else {
        _amountController.text =
            (acc.currentBalance ?? acc.startingBalance ?? 0).toString();
      }

      // Extract Bank Name from Notes
      if (acc.notes != null && acc.notes!.startsWith("Bank: ")) {
        _bankNameController.text = acc.notes!.substring(6);
      }
    } else {
      if (widget.category == AccountCategory.asset) {
        _selectedType = AccountType.checking.apiValue; // Default to Checking
      } else {
        _selectedType = AccountType.creditCard.apiValue;
      }
    }
  }

  @override
  void dispose() {
    _amountController.dispose();
    _nameController.dispose();
    _bankNameController.dispose();
    _lastFourController.dispose();
    _creditLimitController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.background,
      body: SafeArea(
        child: Column(
          children: [
            _buildHeader(),
            Expanded(
              child: SingleChildScrollView(
                padding: const EdgeInsets.symmetric(horizontal: 24),
                child: Form(
                  key: _formKey,
                  child: Column(
                    children: [
                      _isLiability
                          ? _buildCreditCardLayout()
                          : _buildBankAccountLayout(),
                      const SizedBox(height: 32),
                      _buildActionButtons(),
                      const SizedBox(height: 32),
                    ],
                  ),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.only(left: 24, right: 24, top: 48, bottom: 24),
      color: AppColors.background.withValues(
        alpha: 0.9,
      ), // Glass effect simulation
      child: Row(
        children: [
          _buildBackButton(),
          const SizedBox(width: 16),
          Text(
            widget.account != null
                ? (_isLiability ? 'Edit Credit Card' : 'Edit Bank Account')
                : (_isLiability ? 'Add Credit Card' : 'Add Bank Account'),
            style: GoogleFonts.plusJakartaSans(
              fontSize: 24,
              fontWeight: FontWeight.bold,
              color: AppColors.textPrimary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildBackButton() {
    return InkWell(
      onTap: () => Navigator.pop(context),
      borderRadius: BorderRadius.circular(16),
      child: Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          color: AppColors.surface,
          borderRadius: BorderRadius.circular(16),
          boxShadow: AppShadows.shadowSm,
        ),
        child: const Icon(Icons.arrow_back, color: AppColors.textPrimary),
      ),
    );
  }

  // ---------------- BANK ACCOUNT LAYOUT ----------------

  Widget _buildBankAccountLayout() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _buildAmountCard(
          label: 'Current Balance',
          subtitle: 'Current net balance of the account',
        ),
        const SizedBox(height: 24),
        Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(32),
            boxShadow: AppShadows.shadowSm,
          ),
          child: Column(
            children: [
              _buildTextField(
                label: 'Account Name',
                controller: _nameController,
                placeholder: 'e.g. Main Checking',
              ),
              const SizedBox(height: 24),
              _buildAccountTypeSelector(),
              const SizedBox(height: 24),
              _buildTextField(
                label: 'Bank Name',
                controller: _bankNameController,
                placeholder: 'e.g. Chase, Wells Fargo',
                icon: Icons.business,
              ),
              const SizedBox(height: 24),
              _buildLastFourField(),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildAccountTypeSelector() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Account Type',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: AppColors.textPrimary,
          ),
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: _buildRadioItem(
                'Checking',
                Icons.account_balance,
                AccountType.checking.apiValue,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildRadioItem(
                'Savings',
                Icons.savings,
                AccountType.savings.apiValue,
              ),
            ),
          ],
        ),
        const SizedBox(height: 12),
        Row(
          children: [
            Expanded(
              child: _buildRadioItem(
                'Cash',
                Icons.account_balance_wallet,
                AccountType.cash.apiValue,
              ),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: _buildRadioItem(
                'Other',
                Icons.other_houses,
                AccountType.unknown.apiValue,
              ),
            ),
          ],
        ),
      ],
    );
  }

  Widget _buildRadioItem(String label, IconData icon, String value) {
    final isSelected = _selectedType == value;
    return GestureDetector(
      onTap: () => setState(() => _selectedType = value),
      child: Container(
        padding: const EdgeInsets.all(12),
        decoration: BoxDecoration(
          color: isSelected
              ? AppColors.primary.withValues(alpha: 0.1)
              : AppColors.background,
          border: Border.all(
            color: isSelected ? AppColors.primary : Colors.transparent,
            width: 2,
          ),
          borderRadius: BorderRadius.circular(16),
        ),
        child: Column(
          children: [
            Icon(
              icon,
              size: 28,
              color: isSelected ? AppColors.primary : AppColors.textSecondary,
            ),
            const SizedBox(height: 4),
            Text(
              label,
              style: GoogleFonts.plusJakartaSans(
                fontSize: 12,
                fontWeight: FontWeight.bold,
                color: isSelected ? AppColors.primary : AppColors.textSecondary,
              ),
            ),
          ],
        ),
      ),
    );
  }

  // ---------------- CREDIT CARD LAYOUT ----------------

  Widget _buildCreditCardLayout() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.stretch,
      children: [
        _buildAmountCard(
          label: 'Current Outstanding',
          subtitle: 'Total amount currently owed on this card',
        ),
        const SizedBox(height: 24),
        Container(
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: AppColors.surface,
            borderRadius: BorderRadius.circular(32),
            boxShadow: AppShadows.shadowSm,
          ),
          child: Column(
            children: [
              _buildTextField(
                label: 'Account Name',
                controller: _nameController,
                placeholder: 'e.g. Visa Rewards',
              ),
              const SizedBox(height: 24),
              _buildTextField(
                label: 'Bank Name',
                controller: _bankNameController,
                placeholder: 'e.g. Capital One',
                icon: Icons.business,
              ),
              const SizedBox(height: 24),
              _buildLastFourField(),
              const SizedBox(height: 24),
              _buildTextField(
                label: 'Credit Limit',
                controller: _creditLimitController,
                placeholder: 'e.g. 50000',
                icon: Icons.speed, // best approximation for gauge/limit
                inputType: TextInputType.number,
              ),
              const SizedBox(height: 24),
              _buildDropdownField(
                label: 'Statement Day',
                value: _statementDay,
                items: _generateDayOptions(),
                onChanged: (v) => setState(() => _statementDay = v),
              ),
              const SizedBox(height: 24),
              _buildDropdownField(
                label: 'Due Day',
                value: _dueDay,
                items: _generateDayOptions(),
                onChanged: (v) => setState(() => _dueDay = v),
              ),
            ],
          ),
        ),
      ],
    );
  }

  // ---------------- SHARED COMPONENTS ----------------

  Widget _buildAmountCard({required String label, required String subtitle}) {
    return Container(
      padding: const EdgeInsets.all(24),
      decoration: BoxDecoration(
        color: AppColors.surface,
        borderRadius: BorderRadius.circular(32),
        boxShadow: AppShadows.shadowSm,
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label.toUpperCase(),
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w600,
              color: AppColors.textSecondary,
              letterSpacing: 1.0,
            ),
          ),
          const SizedBox(height: 16),
          Row(
            crossAxisAlignment: CrossAxisAlignment.center,
            children: [
              Text(
                '₹',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 32,
                  fontWeight: FontWeight.bold,
                  color: AppColors.primary,
                ),
              ),
              const SizedBox(width: 8),
              Expanded(
                child: TextFormField(
                  controller: _amountController,
                  keyboardType: const TextInputType.numberWithOptions(
                    decimal: true,
                  ),
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 40,
                    fontWeight: FontWeight.bold,
                    color: AppColors.textPrimary,
                  ),
                  decoration: const InputDecoration(
                    border: InputBorder.none,
                    hintText: '0.00',
                    hintStyle: TextStyle(color: AppColors.textPlaceholder),
                    isDense: true,
                    contentPadding: EdgeInsets.zero,
                  ),
                  validator: (value) =>
                      (value == null || value.isEmpty) ? 'Required' : null,
                ),
              ),
            ],
          ),
          const SizedBox(height: 8),
          Container(height: 2, color: AppColors.primary.withValues(alpha: 0.2)),
          const SizedBox(height: 12),
          Text(
            subtitle,
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              color: AppColors.textSecondary,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildTextField({
    required String label,
    required TextEditingController controller,
    String? placeholder,
    IconData? icon,
    TextInputType inputType = TextInputType.text,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: AppColors.textPrimary,
          ),
        ),
        const SizedBox(height: 8),
        Container(
          decoration: BoxDecoration(
            // Use transparent container as the padding logic is inside input decoration if needed,
            // but here we want a specific bg
          ),
          child: Stack(
            alignment: Alignment.centerLeft,
            children: [
              if (icon != null)
                Positioned(
                  left: 16,
                  child: Icon(icon, color: AppColors.textSecondary),
                ),
              TextFormField(
                controller: controller,
                keyboardType: inputType,
                style: GoogleFonts.plusJakartaSans(
                  color: AppColors.textPrimary,
                ),
                decoration: InputDecoration(
                  filled: true,
                  fillColor: AppColors.background,
                  hintText: placeholder,
                  hintStyle: GoogleFonts.plusJakartaSans(
                    color: AppColors.textSecondary,
                  ),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: BorderSide.none,
                  ),
                  contentPadding: EdgeInsets.only(
                    top: 16,
                    bottom: 16,
                    left: 16,
                    right: 16,
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: const BorderSide(
                      color: AppColors.primary,
                      width: 2,
                    ),
                  ),
                ),
                validator: (value) =>
                    (value == null || value.isEmpty) ? 'Required' : null,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildLastFourField() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          'Account Number (Last 4 digits)',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            fontWeight: FontWeight.bold,
            color: AppColors.textPrimary,
          ),
        ),
        const SizedBox(height: 8),
        Row(
          children: [
            Expanded(
              child: Container(
                padding: const EdgeInsets.symmetric(
                  horizontal: 16,
                  vertical: 16,
                ),
                decoration: BoxDecoration(
                  color: AppColors.background,
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: List.generate(
                    3,
                    (index) => Text(
                      '••••',
                      style: TextStyle(
                        fontFamily: 'monospace',
                        color: AppColors.textSecondary.withValues(alpha: 0.5),
                        fontSize: 18,
                      ),
                    ),
                  ),
                ),
              ),
            ),
            const SizedBox(width: 12),
            SizedBox(
              width: 100,
              child: TextFormField(
                controller: _lastFourController,
                maxLength: 4,
                textAlign: TextAlign.center,
                keyboardType: TextInputType.number,
                style: const TextStyle(
                  fontFamily: 'monospace',
                  fontSize: 18,
                  fontWeight: FontWeight.bold,
                  letterSpacing: 2,
                ),
                decoration: InputDecoration(
                  counterText: '',
                  filled: true,
                  fillColor: AppColors.background,
                  hintText: '0000',
                  hintStyle: const TextStyle(
                    fontFamily: 'monospace',
                    color: AppColors.textPlaceholder,
                  ),
                  border: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: BorderSide.none,
                  ),
                  focusedBorder: OutlineInputBorder(
                    borderRadius: BorderRadius.circular(16),
                    borderSide: const BorderSide(
                      color: AppColors.primary,
                      width: 2,
                    ),
                  ),
                  contentPadding: const EdgeInsets.symmetric(vertical: 16),
                ),
                validator: (value) {
                  if (value == null || value.length != 4) return 'Invalid';
                  return null;
                },
              ),
            ),
          ],
        ),
      ],
    );
  }

  List<String> _generateDayOptions() {
    return List.generate(31, (index) => 'Day ${index + 1} of month');
  }

  Widget _buildDropdownField({
    required String label,
    required String? value,
    required List<String> items,
    required Function(String?) onChanged,
  }) {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Text(
          label,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 12,
            fontWeight: FontWeight.bold,
            color: AppColors.textSecondary,
          ),
        ),
        const SizedBox(height: 8),
        Container(
          padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 4),
          decoration: BoxDecoration(
            color: AppColors.background,
            borderRadius: BorderRadius.circular(16),
          ),
          child: DropdownButtonHideUnderline(
            child: DropdownButton<String>(
              value: value,
              isExpanded: true,
              hint: Text(
                'Select Day',
                style: GoogleFonts.plusJakartaSans(
                  color: AppColors.textPlaceholder,
                  fontSize: 14,
                ),
              ),
              icon: const Icon(Icons.arrow_drop_down, color: AppColors.primary),
              items: items.map((String item) {
                return DropdownMenuItem<String>(
                  value: item,
                  child: Text(
                    item,
                    style: GoogleFonts.plusJakartaSans(
                      color: AppColors.textPrimary,
                      fontSize: 14,
                    ),
                  ),
                );
              }).toList(),
              onChanged: onChanged,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildActionButtons() {
    return SizedBox(
      width: double.infinity,
      height: 56,
      child: ElevatedButton(
        onPressed: _isSubmitting ? null : _saveAccount,
        style: ElevatedButton.styleFrom(
          backgroundColor: AppColors.primary,
          foregroundColor: Colors.white,
          elevation: 4,
          shape: RoundedRectangleBorder(
            borderRadius: BorderRadius.circular(16),
          ),
          shadowColor: AppColors.primary.withValues(alpha: 0.4),
        ),
        child: _isSubmitting
            ? const SizedBox(
                width: 24,
                height: 24,
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
                    widget.account != null
                        ? (_isLiability
                              ? "Update Credit Card"
                              : "Update Account")
                        : (_isLiability ? "Save Credit Card" : "Save Account"),
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 18,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                ],
              ),
      ),
    );
  }

  // ---------------- LOGIC ----------------

  Future<void> _saveAccount() async {
    if (!_formKey.currentState!.validate()) return;

    if (_isLiability && (_statementDay == null || _dueDay == null)) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(content: Text('Please select statement and due dates')),
      );
      return;
    }

    setState(() => _isSubmitting = true);

    try {
      final amount = double.tryParse(_amountController.text) ?? 0.0;

      // Notes: Append Bank Name if present
      String finalNotes = "";
      if (_bankNameController.text.isNotEmpty) {
        finalNotes = "Bank: ${_bankNameController.text.trim()}";
      }

      final startingBalance = _isLiability ? 0.0 : amount;
      final currentOutstanding = _isLiability ? amount : 0.0;

      // Credit Card specific format
      final cutOffDay = _isLiability && _statementDay != null
          ? _statementDay!
          : 'Day 1 of month';
      final dueDay = _isLiability && _dueDay != null
          ? _dueDay!
          : 'Day 1 of month';

      final creditLimit = double.tryParse(_creditLimitController.text) ?? 0.0;

      final request = AccountCreateUpdateRequest(
        accountName: _nameController.text.trim(),
        lastFour: int.parse(_lastFourController.text),
        accountType: _selectedType, // CHECKING/SAVINGS/CASH/CREDIT CARD
        openingDate: _openingDate,
        startingBalance: startingBalance,
        currentOutstanding: currentOutstanding,
        currency: _selectedCurrency,
        creditLimit: creditLimit,
        statementDayOfMonth: cutOffDay,
        dueDayOfMonth: dueDay,
        notes: finalNotes,
        hideFromSelection: false, // Default false
        hideFromReports: false, // Default false
        category: _isLiability ? 'LIABILITY' : 'ASSET',
      );

      if (widget.account != null) {
        await ref
            .read(accountsControllerProvider.notifier)
            .updateAccount(id: widget.account!.id, request: request);
      } else {
        await ref.read(createAccountProvider(request).future);
      }

      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(
            widget.account != null
                ? 'Account updated successfully!'
                : 'Account added successfully!',
          ),
          backgroundColor: AppColors.success,
        ),
      );
      Navigator.pop(context, true);
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text('Failed to save account: $e'),
          backgroundColor: AppColors.error,
        ),
      );
    } finally {
      if (mounted) setState(() => _isSubmitting = false);
    }
  }
}
