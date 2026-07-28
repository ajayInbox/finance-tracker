import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:finance_app/features/account/application/accounts_controller.dart';
import 'package:finance_app/features/account/data/model/account_category.dart';
import 'package:finance_app/features/account/ui/add_account_page.dart';

enum AppExperienceMode { quickLedger, fullFinance }

class OnboardingModeDialog extends ConsumerWidget {
  const OnboardingModeDialog({super.key});

  static Future<AppExperienceMode?> show(BuildContext context) {
    return showModalBottomSheet<AppExperienceMode>(
      context: context,
      isDismissible: false,
      enableDrag: false,
      isScrollControlled: true,
      backgroundColor: Colors.transparent,
      builder: (context) => PopScope(
        canPop: false,
        child: const OnboardingModeDialog(),
      ),
    );
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final theme = Theme.of(context);
    final surfaceColor = theme.colorScheme.surface;
    final textColor = theme.textTheme.titleLarge?.color ?? Colors.black;
    final subtitleColor = theme.textTheme.bodySmall?.color ?? Colors.grey;

    return Container(
      decoration: BoxDecoration(
        color: surfaceColor,
        borderRadius: const BorderRadius.vertical(top: Radius.circular(36)),
      ),
      padding: EdgeInsets.fromLTRB(24, 20, 24, MediaQuery.of(context).padding.bottom + 28),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Center(
            child: Container(
              width: 44,
              height: 5,
              decoration: BoxDecoration(
                color: theme.dividerColor,
                borderRadius: BorderRadius.circular(999),
              ),
            ),
          ),
          const SizedBox(height: 24),
          Text(
            'Choose your experience',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 24,
              fontWeight: FontWeight.w800,
              color: textColor,
              letterSpacing: -0.5,
            ),
          ),
          const SizedBox(height: 8),
          Text(
            'How would you like to use Personal Finance Tracker? Choose Mode 1 for simple expense logging or Mode 2 for full bank account tracking.',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 14,
              fontWeight: FontWeight.w500,
              color: subtitleColor,
              height: 1.45,
            ),
          ),
          const SizedBox(height: 24),
          _buildModeOption(
            context: context,
            title: 'Mode 1: Quick Expense Ledger',
            subtitle: 'Log daily cash & expenses instantly with zero manual bank setup.',
            tag: 'RECOMMENDED FOR SIMPLE LOGGING',
            tagColor: theme.colorScheme.primary,
            icon: Icons.bolt_rounded,
            iconBg: theme.colorScheme.primary.withValues(alpha: 0.12),
            onTap: () async {
              await ref.read(accountsControllerProvider.notifier).initializeDefaults();
              if (context.mounted) {
                Navigator.pop(context, AppExperienceMode.quickLedger);
              }
            },
          ),
          const SizedBox(height: 16),
          _buildModeOption(
            context: context,
            title: 'Mode 2: Full Personal Finance Manager',
            subtitle: 'Add your Primary Bank Account upfront to track Net Worth & Bank Accounts.',
            tag: 'MANDATORY BANK SETUP',
            tagColor: const Color(0xFF3B82F6),
            icon: Icons.account_balance_rounded,
            iconBg: const Color(0xFF3B82F6).withValues(alpha: 0.12),
            onTap: () async {
              Navigator.pop(context, AppExperienceMode.fullFinance);
              await Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (context) => const AddAccountPage(
                    category: AccountCategory.asset,
                  ),
                ),
              );
            },
          ),
          const SizedBox(height: 12),
        ],
      ),
    );
  }

  Widget _buildModeOption({
    required BuildContext context,
    required String title,
    required String subtitle,
    required String tag,
    required Color tagColor,
    required IconData icon,
    required Color iconBg,
    required VoidCallback onTap,
  }) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: Ink(
        padding: const EdgeInsets.all(20),
        decoration: BoxDecoration(
          color: theme.scaffoldBackgroundColor,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: theme.dividerColor),
        ),
        child: Row(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Container(
              width: 48,
              height: 48,
              decoration: BoxDecoration(
                color: iconBg,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Icon(icon, color: tagColor, size: 26),
            ),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                    decoration: BoxDecoration(
                      color: tagColor.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(999),
                    ),
                    child: Text(
                      tag,
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 10,
                        fontWeight: FontWeight.w800,
                        color: tagColor,
                        letterSpacing: 0.6,
                      ),
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    title,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: theme.textTheme.titleLarge?.color ?? Colors.black,
                    ),
                  ),
                  const SizedBox(height: 4),
                  Text(
                    subtitle,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: theme.textTheme.bodySmall?.color ?? Colors.grey,
                      height: 1.35,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}
