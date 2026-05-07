import 'package:finance_app/features/account/data/model/account.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:intl/intl.dart';

class AccountDetailsSheet extends StatelessWidget {
  final Account account;
  final VoidCallback onEdit;
  final VoidCallback onDelete;

  const AccountDetailsSheet({
    super.key,
    required this.account,
    required this.onEdit,
    required this.onDelete,
  });

  @override
  Widget build(BuildContext context) {
    // Determine account specific styling/icons based on type if needed,
    // but for now relying on the standard Bank Account style from spec.
    final primaryColor = Theme.of(context).colorScheme.primary; // Emerald green from spec

    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.vertical(top: Radius.circular(40)),
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        children: [
          // Handle bar
          SizedBox(height: 16),
          Container(
            width: 48,
            height: 6,
            decoration: BoxDecoration(
              color: Theme.of(context).dividerColor,
              borderRadius: BorderRadius.circular(3),
            ),
          ),
          SizedBox(height: 24),

          // Header
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 24),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Account Details',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 20,
                    fontWeight: FontWeight.bold,
                    color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                  ),
                ),
                InkWell(
                  onTap: () => Navigator.pop(context),
                  borderRadius: BorderRadius.circular(20),
                  child: Container(
                    width: 32,
                    height: 32,
                    decoration: BoxDecoration(
                      color: Theme.of(context).scaffoldBackgroundColor,
                      shape: BoxShape.circle,
                    ),
                    child: Icon(
                      Icons.close,
                      size: 18,
                      color: Color(0xFF6B7280),
                    ),
                  ),
                ),
              ],
            ),
          ),
          SizedBox(height: 32),

          // Balance Circle
          Container(
            width: 80,
            height: 80,
            decoration: BoxDecoration(
              color: const Color(0xFFEFF6FF), // blue-50
              borderRadius: BorderRadius.circular(32),
            ),
            child: Icon(
              Icons.account_balance,
              size: 40,
              color: Color(0xFF2563EB), // text-blue-600
            ),
          ),
          SizedBox(height: 16),
          Text(
            'Current Balance',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 14,
              fontWeight: FontWeight.w600,
              color: const Color(0xFF6B7280),
            ),
          ),
          SizedBox(height: 4),
          Row(
            mainAxisAlignment: MainAxisAlignment.center,
            crossAxisAlignment: CrossAxisAlignment.baseline,
            textBaseline: TextBaseline.alphabetic,
            children: [
              Text(
                '₹ ${NumberFormat('#,##,###').format(account.effectiveBalance.floor())}',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 36,
                  fontWeight: FontWeight.w800,
                  color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                  letterSpacing: -1,
                ),
              ),
              Text(
                '.${(account.effectiveBalance * 100 % 100).toStringAsFixed(0).padLeft(2, '0')}',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 20,
                  fontWeight: FontWeight.w600,
                  color: const Color(0xFF6B7280),
                ),
              ),
            ],
          ),
          SizedBox(height: 32),

          // Details Card
          Container(
            margin: EdgeInsets.symmetric(horizontal: 24),
            padding: EdgeInsets.all(20),
            decoration: BoxDecoration(
              color: const Color(0xFFF9FAFB),
              borderRadius: BorderRadius.circular(24),
              border: Border.all(color: Theme.of(context).scaffoldBackgroundColor),
            ),
            child: Column(
              children: [
                _buildDetailRow(context, 'Account Name', account.accountName),
                _buildDivider(),
                _buildDetailRow(context, 'Bank Name', _extractBankName(account.notes)),
                _buildDivider(),
                _buildDetailRow(
                  context,
                  'Account Type',
                  _formatAccountType(account.accountType),
                ),
                _buildDivider(),
                _buildDetailRow(
                  context,
                  'Account Number',
                  '**** ${account.lastFour}',
                  isMono: true,
                ),
              ],
            ),
          ),
          SizedBox(height: 32),

          // Actions
          Padding(
            padding: EdgeInsets.symmetric(horizontal: 24),
            child: Column(
              children: [
                SizedBox(
                  width: double.infinity,
                  height: 56,
                  child: ElevatedButton(
                    onPressed: onEdit,
                    style: ElevatedButton.styleFrom(
                      backgroundColor: primaryColor,
                      foregroundColor: Theme.of(context).colorScheme.surface,
                      elevation: 0,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                      shadowColor: primaryColor.withValues(alpha: 0.4),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.edit, size: 20),
                        SizedBox(width: 8),
                        Text(
                          'Edit Bank Account',
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
                SizedBox(height: 12),
                SizedBox(
                  width: double.infinity,
                  height: 56,
                  child: TextButton(
                    onPressed: onDelete,
                    style: TextButton.styleFrom(
                      backgroundColor: Theme.of(context).colorScheme.error.withValues(alpha: 0.1), // red-50
                      foregroundColor: const Color(0xFFDC2626), // red-600
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(16),
                      ),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.center,
                      children: [
                        Icon(Icons.delete_outline, size: 20),
                        SizedBox(width: 8),
                        Text(
                          'Delete Bank Account',
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 16,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                  ),
                ),
              ],
            ),
          ),
          SizedBox(height: 48),
        ],
      ),
    );
  }

  Widget _buildDetailRow(BuildContext context, String label, String value, {bool isMono = false}) {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            label,
            style: GoogleFonts.plusJakartaSans(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: const Color(0xFF6B7280),
            ),
          ),
          Text(
            value,
            style: isMono
                ? GoogleFonts.robotoMono(
                    // Using RobotoMono for monospace needs
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                    letterSpacing: 1,
                  )
                : GoogleFonts.plusJakartaSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                  ),
          ),
        ],
      ),
    );
  }

  Widget _buildDivider() {
    return Padding(
      padding: EdgeInsets.symmetric(vertical: 12),
      child: Container(height: 1, color: const Color(0xFFE5E7EB)),
    );
  }

  // Helper to extract bank name from notes if stored there (as per AddAccountPage logic)
  String _extractBankName(String? notes) {
    if (notes != null && notes.startsWith("Bank: ")) {
      return notes.substring(6);
    }
    return 'Unknown Bank';
  }

  String _formatAccountType(dynamic type) {
    // Simple capitalization
    final typeStr = type.toString().split('.').last;
    if (typeStr.isEmpty) return typeStr;
    return typeStr[0].toUpperCase() + typeStr.substring(1).toLowerCase();
  }
}
