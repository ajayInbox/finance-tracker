import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:intl/intl.dart';
import 'package:finance_app/features/transaction/data/model/transaction_summary.dart';
import 'package:finance_app/utils/category_icon.dart';
import 'package:finance_app/features/category/application/category_controller.dart';

class TransactionCard extends ConsumerWidget {
  final TransactionSummary transaction;
  final VoidCallback? onTap;

  const TransactionCard({
    super.key,
    required this.transaction,
    this.onTap,
  });

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final isIncome = transaction.type.toLowerCase() == "income";
    final categoriesAsync = ref.watch(childrenCategoriesProvider);

    IconData icon = Icons.receipt;
    Color iconColor = const Color(0xFF6B7280);
    Color bgIconColor = Theme.of(context).scaffoldBackgroundColor;

    categoriesAsync.whenData((categories) {
      try {
        final category = categories.firstWhere(
          (c) => c.name.toLowerCase() == transaction.categoryName.toLowerCase(),
        );
        icon = CategoryIcons.parseIcon(category.iconKey);
        iconColor = CategoryIcons.parseColor(category.colorCode);
        bgIconColor = iconColor.withValues(alpha: 0.15);
      } catch (e) {
        // Fallback for unmatched category name
        if (isIncome) {
          iconColor = const Color(0xFF22C55E); // green-500
          bgIconColor = const Color(0xFFDCFCE7); // green-100
          icon = Icons.account_balance;
        } else {
          icon = CategoryIcons.parseIcon('');
        }
      }
    });

    return Container(
      margin: EdgeInsets.only(bottom: 12),
      child: InkWell(
        onTap: onTap,
        borderRadius: BorderRadius.circular(16),
        child: Container(
          padding: EdgeInsets.all(16),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surface,
            borderRadius: BorderRadius.circular(16), // rounded-2xl
            border: Border.all(color: const Color(0xFFE5E7EB)),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.04), // shadow-soft
                blurRadius: 40,
                offset: const Offset(0, 10),
              ),
            ],
          ),
          child: Row(
            children: [
              Container(
                width: 48,
                height: 48,
                decoration: BoxDecoration(
                  color: bgIconColor,
                  borderRadius: BorderRadius.circular(12), // rounded-xl
                ),
                child: Icon(icon, color: iconColor, size: 24),
              ),
              SizedBox(width: 16),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Expanded(
                          child: Text(
                            transaction.transactionName,
                            style: GoogleFonts.plusJakartaSans(
                              fontWeight: FontWeight.w700,
                              fontSize: 16,
                              color: Theme.of(context).textTheme.titleLarge?.color ?? Colors.black,
                            ),
                            maxLines: 1,
                            overflow: TextOverflow.ellipsis,
                          ),
                        ),
                        SizedBox(width: 8),
                        Text(
                          '${isIncome ? '+' : '-'} ₹${transaction.amount.abs().toStringAsFixed(2)}',
                          style: GoogleFonts.plusJakartaSans(
                            fontWeight: FontWeight.w700,
                            fontSize: 16,
                            color: isIncome
                                ? Theme.of(context).colorScheme.primary
                                : Theme.of(context).textTheme.titleLarge?.color,
                          ),
                        ),
                      ],
                    ),
                    SizedBox(height: 4),
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        Text(
                          transaction.categoryName,
                          style: GoogleFonts.plusJakartaSans(
                            color: const Color(0xFF6B7280),
                            fontSize: 14,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                        Text(
                          _formatDate(transaction.occurredAt),
                          style: GoogleFonts.plusJakartaSans(
                            color: const Color(0xFF9CA3AF),
                            fontSize: 12,
                            fontWeight: FontWeight.w500,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }

  String _formatDate(DateTime date) {
    final now = DateTime.now();
    final difference = now.difference(date);

    if (difference.inDays == 0) {
      return 'Today, ${DateFormat('h:mm a').format(date)}';
    } else if (difference.inDays == 1) {
      return 'Yesterday';
    } else if (difference.inDays < 7) {
      return DateFormat('EEEE').format(date);
    } else {
      return DateFormat('MMM d').format(date);
    }
  }
}
