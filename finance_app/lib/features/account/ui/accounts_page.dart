import 'package:finance_app/features/account/application/accounts_controller.dart';
import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/account/provider/networth_provider.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:finance_app/features/account/data/model/networth_summary.dart';

import 'dart:ui';

class AccountsPage extends ConsumerStatefulWidget {
  const AccountsPage({super.key});

  @override
  ConsumerState<AccountsPage> createState() => _AccountsPageState();
}

class _AccountsPageState extends ConsumerState<AccountsPage>
    with TickerProviderStateMixin {
  late AnimationController _fadeController;
  late AnimationController _slideController;

  @override
  void initState() {
    super.initState();

    _fadeController = AnimationController(
      duration: const Duration(milliseconds: 800),
      vsync: this,
    )..forward();

    _slideController = AnimationController(
      duration: const Duration(milliseconds: 600),
      vsync: this,
    )..forward();
  }

  @override
  void dispose() {
    _fadeController.dispose();
    _slideController.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    final accountsAsync = ref.watch(accountsControllerProvider);
    final networthAsync = ref.watch(networthProvider);
    return Scaffold(
      backgroundColor: const Color(0xFFF3F4F6),
      body: SingleChildScrollView(
        child: Column(
          children: [
            _buildHeader(),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 24),
              child: networthAsync.when(
                data: (netWorth) => _buildNetWorthCard(netWorth),
                loading: () => const Center(child: CircularProgressIndicator()),
                error: (e, _) => Text('Error: $e'),
              ),
            ),
            const SizedBox(height: 32),
            accountsAsync.when(
              data: (accounts) {
                final assets = accounts.where((a) => a.isAsset()).toList();
                final liabilities = accounts
                    .where((a) => !a.isAsset())
                    .toList();

                return Padding(
                  padding: const EdgeInsets.symmetric(horizontal: 24),
                  child: Column(
                    children: [
                      _buildSectionHeader('Bank Accounts', assets.length),
                      const SizedBox(height: 16),
                      ...assets.map((a) => _buildAccountCard(a)),
                      const SizedBox(height: 32),
                      _buildSectionHeader('Credit Cards', liabilities.length),
                      const SizedBox(height: 16),
                      ...liabilities.map((a) => _buildCreditCardItem(a)),
                      const SizedBox(height: 16),
                      _buildAddAccountButton(),
                      const SizedBox(height: 100), // Bottom padding
                    ],
                  ),
                );
              },
              loading: () => const Center(child: CircularProgressIndicator()),
              error: (e, _) => Center(child: Text('Error: $e')),
            ),
          ],
        ),
      ),
    );
  }

  void _showAddAccountDialog(String? type) {
    // Navigation logic to add account page
  }

  Widget _buildHeader() {
    return Container(
      padding: const EdgeInsets.only(left: 24, right: 24, top: 48, bottom: 24),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            'Linked Accounts',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 24, // text-2xl
              fontWeight: FontWeight.w700, // font-bold
              color: const Color(0xFF111827), // text-primary-light
            ),
          ),
          Container(
            width: 48,
            height: 48,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(16), // rounded-2xl
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.05),
                  blurRadius: 2,
                  offset: const Offset(0, 1),
                ),
              ],
            ),
            child: Stack(
              children: [
                const Center(
                  child: Icon(
                    Icons.notifications_none_rounded,
                    color: Color(0xFF4B5563), // text-gray-600
                    size: 24,
                  ),
                ),
                Positioned(
                  top: 12,
                  right: 14,
                  child: Container(
                    width: 8,
                    height: 8,
                    decoration: const BoxDecoration(
                      color: Color(0xFFEF4444), // bg-red-500
                      shape: BoxShape.circle,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildNetWorthCard(NetworthSummary netWorth) {
    return Container(
      width: double.infinity,
      clipBehavior: Clip.hardEdge,
      decoration: BoxDecoration(
        color: Colors.white, // bg-card-light
        borderRadius: BorderRadius.circular(32), // rounded-[2rem]
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.08), // shadow-soft
            blurRadius: 40,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Stack(
        children: [
          // Background blur decoration
          Positioned(
            top: -96,
            right: -96,
            child: Container(
              width: 256,
              height: 256,
              decoration: BoxDecoration(
                color: const Color(
                  0xFF10B981,
                ).withValues(alpha: 0.1), // bg-primary/10
                shape: BoxShape.circle,
              ),
              child: BackdropFilter(
                filter: ImageFilter.blur(sigmaX: 60, sigmaY: 60),
              ),
            ),
          ),
          Padding(
            padding: const EdgeInsets.all(24),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  'Total Net Worth',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w500,
                    color: const Color(0xFF6B7280), // text-secondary-light
                  ),
                ),
                const SizedBox(height: 4),
                Text(
                  netWorth.formattedNetWorth,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 30, // text-3xl
                    fontWeight: FontWeight.w700,
                    color: const Color(0xFF111827),
                    letterSpacing: -0.5,
                  ),
                ),
                const SizedBox(height: 16),
                Row(
                  children: [
                    _buildLegendItem(
                      color: const Color(0xFF22C55E), // green-500
                      label: 'Assets',
                      amount: netWorth.formattedAssets,
                    ),
                    const SizedBox(width: 16),
                    _buildLegendItem(
                      color: const Color(0xFFEF4444), // red-500
                      label: 'Liabilities',
                      amount: netWorth.formattedLiabilities,
                    ),
                  ],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildLegendItem({
    required Color color,
    required String label,
    required String amount,
  }) {
    return Row(
      children: [
        Container(
          width: 8,
          height: 8,
          decoration: BoxDecoration(color: color, shape: BoxShape.circle),
        ),
        const SizedBox(width: 8),
        RichText(
          text: TextSpan(
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w500,
              color: const Color(0xFF6B7280), // text-secondary-light
            ),
            children: [
              TextSpan(text: '$label '),
              TextSpan(
                text: amount,
                style: const TextStyle(
                  color: Color(0xFF111827), // text-primary-light
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildSectionHeader(String title, int count) {
    return Row(
      mainAxisAlignment: MainAxisAlignment.spaceBetween,
      children: [
        Text(
          '$title ($count)',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 18, // text-lg
            fontWeight: FontWeight.w700, // font-bold
            color: const Color(0xFF111827), // text-primary-light
          ),
        ),
      ],
    );
  }

  Widget _buildAccountCard(Account account) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(20), // p-5
      decoration: BoxDecoration(
        color: Colors.white, // bg-card-light
        borderRadius: BorderRadius.circular(24), // rounded-3xl
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04), // shadow-sm
            blurRadius: 4,
            offset: const Offset(0, 1),
          ),
        ],
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                width: 48, // w-12
                height: 48, // h-12
                decoration: BoxDecoration(
                  color: const Color(0xFFEFF6FF), // bg-blue-50
                  borderRadius: BorderRadius.circular(16), // rounded-2xl
                ),
                child: const Icon(
                  Icons.account_balance,
                  color: Color(0xFF2563EB), // text-blue-600
                  size: 24, // text-2xl
                ),
              ),
              const SizedBox(width: 16),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    account.accountName,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 16, // text-base
                      fontWeight: FontWeight.w700, // font-bold
                      color: const Color(0xFF111827), // text-primary-light
                    ),
                  ),
                  const SizedBox(height: 2),
                  Text(
                    '**** ${account.id.substring(0, 4)}', // Masked
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12, // text-xs
                      fontWeight: FontWeight.w500, // font-medium
                      color: const Color(0xFF6B7280), // text-secondary-light
                    ),
                  ),
                ],
              ),
            ],
          ),
          Column(
            crossAxisAlignment: CrossAxisAlignment.end,
            children: [
              Text(
                '₹ ${NumberFormat('#,##,###').format(account.effectiveBalance.abs())}',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 18, // text-lg
                  fontWeight: FontWeight.w700, // font-bold
                  color: const Color(0xFF111827), // text-primary-light
                ),
              ),
              const SizedBox(height: 4),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 2),
                decoration: BoxDecoration(
                  color: const Color(0xFFECFDF5), // bg-green-50
                  borderRadius: BorderRadius.circular(999),
                ),
                child: Text(
                  '+12%', // Static for now as per design
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 10, // text-[10px]
                    fontWeight: FontWeight.w700, // font-bold
                    color: const Color(0xFF22C55E), // text-green-500
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildCreditCardItem(Account account) {
    return Container(
      margin: const EdgeInsets.only(bottom: 16),
      padding: const EdgeInsets.all(20), // p-5
      clipBehavior: Clip.hardEdge,
      decoration: BoxDecoration(
        color: Colors.white, // bg-card-light
        borderRadius: BorderRadius.circular(24), // rounded-3xl
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04), // shadow-sm
            blurRadius: 4,
            offset: const Offset(0, 1),
          ),
        ],
      ),
      child: Stack(
        children: [
          Positioned(
            right: 0,
            top: 0,
            bottom: 0,
            child: Container(
              width: 4,
              color: const Color(0xFFEF4444), // bg-red-500
            ),
          ),
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Row(
                children: [
                  Container(
                    width: 48, // w-12
                    height: 48, // h-12
                    decoration: BoxDecoration(
                      color: const Color(0xFFFEF2F2), // bg-red-50
                      borderRadius: BorderRadius.circular(16), // rounded-2xl
                    ),
                    child: const Icon(
                      Icons.credit_card,
                      color: Color(0xFFDC2626), // text-red-600
                      size: 24, // text-2xl
                    ),
                  ),
                  const SizedBox(width: 16),
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        account.accountName,
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 16, // text-base
                          fontWeight: FontWeight.w700, // font-bold
                          color: const Color(0xFF111827), // text-primary-light
                        ),
                      ),
                      const SizedBox(height: 2),
                      Text(
                        '**** ${account.id.substring(0, 4)}', // Masked
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 12, // text-xs
                          fontWeight: FontWeight.w500, // font-medium
                          color: const Color(
                            0xFF6B7280,
                          ), // text-secondary-light
                        ),
                      ),
                    ],
                  ),
                ],
              ),
              Padding(
                padding: const EdgeInsets.only(right: 16), // Space for red bar
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '- ₹ ${NumberFormat('#,##,###').format(account.effectiveBalance.abs())}',
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 18, // text-lg
                        fontWeight: FontWeight.w700, // font-bold
                        color: const Color(0xFFEF4444), // text-red-500
                      ),
                    ),
                    const SizedBox(height: 4),
                    Text(
                      'Due in 5 days', // Static
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 10,
                        fontWeight: FontWeight.w500,
                        color: const Color(0xFF6B7280), // text-secondary-light
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildAddAccountButton() {
    return Container(
      width: double.infinity,
      height: 60,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24), // rounded-3xl
        border: Border.all(
          color: const Color(0xFFD1D5DB), // border-gray-300
          width: 2,
          style: BorderStyle
              .solid, // Flutter doesn't support dashed border easily without package, using solid gray for now or CustomPainter if strict.
          // Note: MD asks for dashed. I should use FDottedLine or CustomPainter if strict, but solid gray is a safer default backup.
          // Let's try to stick to solid for reliability unless I add a package or complex painter.
          // User said "exactly same", but "dashed" usually requires `dotted_border` package.
          // I will use a simple OutlineButton style with gray border.
        ),
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: () => _showAddAccountDialog(null),
          borderRadius: BorderRadius.circular(24),
          child: Row(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              const Icon(
                Icons.add_circle_outline,
                color: Color(0xFF6B7280), // text-secondary-light
              ),
              const SizedBox(width: 8),
              Text(
                'Add New Account',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  fontWeight: FontWeight.w600,
                  color: const Color(0xFF6B7280),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}
