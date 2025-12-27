import 'package:finance_app/features/account/application/accounts_controller.dart';
import 'package:finance_app/features/account/provider/networth_provider.dart';
import 'package:finance_app/features/account/ui/add_account_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_speed_dial/flutter_speed_dial.dart';
import 'package:intl/intl.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/account/data/model/networth_summary.dart';
import 'package:finance_app/utils/app_style_constants.dart';
import 'package:finance_app/features/account/data/model/account_category.dart';
import 'package:finance_app/features/account/data/model/account_type.dart';

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

  Future<void> _refresh() async {
    await ref
      .read(accountsControllerProvider.notifier)
      .refresh();
    ref.invalidate(networthProvider);
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

    return Scaffold(
      backgroundColor: AppColors.lightBackground,

      floatingActionButton: accountsAsync.maybeWhen(
        data: (accounts) => accounts.isNotEmpty ? _buildFAB() : null,
        orElse: () => null,
      ),

      body: CustomScrollView(
        slivers: [
          SliverToBoxAdapter(
            child: FadeTransition(
              opacity: _fadeController,
              child: SlideTransition(
                position: Tween<Offset>(
                  begin: const Offset(0, 0.10),
                  end: Offset.zero,
                ).animate(_slideController),
                child: RefreshIndicator(
                  onRefresh: _refresh,
                  child: Column(
                    children: [
                      _buildNetworthFuture(),
                      const SizedBox(height: 16),
                      _buildAccountsFuture(),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  // --------------------------------------------------------------------------
  // FAB
  // --------------------------------------------------------------------------

  Widget _buildFAB() {
    return Container(
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF6C63FF), Color(0xFF9C27B0)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(56),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.08),
            blurRadius: 16,
            offset: const Offset(0, 4),
          ),
        ],
      ),
      child: SpeedDial(
        backgroundColor: Colors.transparent,
        elevation: 0,
        icon: Icons.add,
        activeIcon: Icons.close,
        foregroundColor: Colors.white,
        overlayOpacity: 0.35,
        spacing: 8,
        spaceBetweenChildren: 8,
        children: [
          SpeedDialChild(
            child: const Icon(Icons.account_balance_wallet_outlined),
            label: 'Add asset account',
            labelBackgroundColor: Colors.black87,
            labelStyle: const TextStyle(color: Colors.white),
            onTap: () => _openAddAccountPage(AccountCategory.asset),
          ),
          SpeedDialChild(
            child: const Icon(Icons.credit_card),
            label: 'Add liability account',
            labelBackgroundColor: Colors.black87,
            labelStyle: const TextStyle(color: Colors.white),
            onTap: () => _openAddAccountPage(AccountCategory.liability),
          ),
        ],
      ),
    );
  }

  Future<void> _openAddAccountPage(AccountCategory category) async {
    final created = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (_) => AddAccountPage(category: category),
      ),
    );

    // if (created == true) {
    //   if (!mounted) return;
    //   ref.invalidate(accountsProvider);
    //   ref.invalidate(networthProvider);
    // }
  }

  // --------------------------------------------------------------------------
  // NET WORTH
  // --------------------------------------------------------------------------

  Widget _buildNetworthFuture() {
    final networthAsync = ref.watch(networthProvider);

    return networthAsync.when(
      data: (summary) => _buildNetworthCard(summary),
      loading: () => _buildNetworthLoading(),
      error: (_, __) => _buildNetworthError(),
    );
  }

  Widget _buildNetworthCard(NetworthSummary summary) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      padding: const EdgeInsets.symmetric(horizontal: 24, vertical: 28),
      decoration: BoxDecoration(
        gradient: const LinearGradient(
          colors: [Color(0xFF6C63FF), Color(0xFF00B4DB)],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: AppColors.primaryBlue.withOpacity(0.15),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Header
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Total Net Worth',
                style: GoogleFonts.inter(
                  color: Colors.white,
                  fontSize: 14,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.18),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Text(
                  '${summary.assets.number + summary.liabilities.number} Accounts',
                  style: GoogleFonts.inter(
                    color: Colors.white,
                    fontSize: 12,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 16),
          Text(
            summary.formattedNetWorth,
            style: GoogleFonts.inter(
              color: Colors.white,
              fontSize: 32,
              fontWeight: FontWeight.bold,
            ),
          ),
          const SizedBox(height: 20),
          Row(
            children: [
              _buildNetItem('Assets', summary.formattedAssets, AppColors.success),
              _buildNetItem('Liabilities', summary.formattedLiabilities, AppColors.error),
            ],
          )
        ],
      ),
    );
  }

  Widget _buildNetItem(String label, String value, Color color) {
    return Expanded(
      child: Row(
        children: [
          Icon(Icons.circle, size: 10, color: color),
          const SizedBox(width: 6),
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(label,
                  style: GoogleFonts.inter(color: Colors.white70, fontSize: 12)),
              Text(
                value,
                style: GoogleFonts.inter(
                    color: Colors.white, fontSize: 18, fontWeight: FontWeight.w600),
              ),
            ],
          )
        ],
      ),
    );
  }

  // --------------------------------------------------------------------------
  // ACCOUNT LIST
  // --------------------------------------------------------------------------

  Widget _buildAccountsFuture() {
    final accountsAsync = ref.watch(accountsControllerProvider);

    return accountsAsync.when(
      data: (accounts) =>
      accounts.isEmpty ? _buildEmptyState() : _buildAccountsList(accounts),
      loading: () => _buildLoadingList(),
      error: (_, __) => _buildErrorList(),
    );
  }

  Widget _buildEmptyState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            padding: const EdgeInsets.all(16.0),
            decoration: BoxDecoration(
              color: AppColors.textSecondary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(50.0),
            ),
            child: Icon(
              Icons.account_balance_wallet,
              color: AppColors.textSecondary,
              size: 64.0,
            ),
          ),
          const SizedBox(height: 16.0),
          Text(
            'No accounts yet',
            style: GoogleFonts.inter(
              color: AppColors.textSecondary,
              fontSize: 18.0,
              fontWeight: AppTypography.weightSemibold,
            ),
          ),
          const SizedBox(height: 8.0),
          Text(
            'Add your first account to start tracking',
            style: GoogleFonts.inter(
              color: AppColors.textSecondary.withValues(alpha: 0.8),
              fontSize: AppTypography.body,
            ),
          ),
          const SizedBox(height: 32.0),
          ElevatedButton.icon(
            onPressed: () => _showAccountTypeSelector(),
            icon: Icon(
              Icons.add,
              size: 20.0,
              color: Colors.white,
            ),
            label: Text(
              'Add Account',
              style: GoogleFonts.inter(
                fontSize: AppTypography.body,
                fontWeight: AppTypography.weightMedium,
              ),
            ),
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primaryBlue,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 32.0, vertical: 12.0),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppBorderRadius.medium),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAccountsList(List<Account> accounts) {
    final assets = accounts.where((acc) => acc.isAsset()).toList();
    final liabilities = accounts.where((acc) => !acc.isAsset()).toList();

    return ListView(
      shrinkWrap: true,
      physics: const NeverScrollableScrollPhysics(),
      padding: const EdgeInsets.symmetric(horizontal: 16),
      children: [
        if (assets.isNotEmpty) ...[
          _buildSectionHeader("Assets", assets.length),
          ...assets.map(_buildAccountCard),
        ],
        if (liabilities.isNotEmpty) ...[
          _buildSectionHeader("Liabilities", liabilities.length),
          ...liabilities.map(_buildAccountCard),
        ],
      ],
    );
  }

  Widget _buildSectionHeader(String title, int count) {
    return Padding(
      padding: const EdgeInsets.only(top: 20, bottom: 8),
      child: Row(
        children: [
          Container(
            width: 3,
            height: 22,
            decoration: BoxDecoration(
              color: title == "Assets" ? AppColors.success : AppColors.error,
              borderRadius: BorderRadius.circular(2),
            ),
          ),
          const SizedBox(width: 8),
          Text(
            "$title ($count)",
            style: GoogleFonts.inter(fontSize: 18, fontWeight: FontWeight.w600),
          ),
          const Spacer(),
          TextButton(
            onPressed: () {},
            child: Row(
              children: [
                Text("View All", style: GoogleFonts.inter(color: AppColors.primaryBlue)),
                const SizedBox(width: 4),
                Icon(Icons.arrow_forward_ios, size: 12, color: AppColors.primaryBlue),
              ],
            ),
          )
        ],
      ),
    );
  }

  // --------------------------------------------------------------------------
  // ACCOUNT CARD (REFINED FOR ENUM MODEL)
  // --------------------------------------------------------------------------

  Widget _buildAccountCard(Account account) {
    final isAsset = account.isAsset();
    final color = isAsset ? AppColors.success : AppColors.error;
    final balance = account.effectiveBalance;

    return Container(
      margin: const EdgeInsets.only(bottom: 12),
      decoration: BoxDecoration(
        color: AppColors.lightSurface,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: AppColors.cardBorder),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.04),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: InkWell(
        borderRadius: BorderRadius.circular(16),
        onTap: () {},
        child: Padding(
          padding: const EdgeInsets.all(16),
          child: Row(
            children: [
              Container(
                width: 48,
                height: 48,
                padding: const EdgeInsets.all(12),
                decoration: BoxDecoration(
                  color: color.withValues(alpha: 0.12),
                  borderRadius: BorderRadius.circular(10),
                ),
                child: Icon(
                  _getAccountIcon(account.accountType),
                  color: color,
                  size: 22,
                ),
              ),
              const SizedBox(width: 16),

              // Name & Type
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(account.accountName,
                        style: GoogleFonts.inter(
                            fontWeight: FontWeight.w600, fontSize: 16)),

                    const SizedBox(height: 4),
                    Text(
                      account.accountType.label,
                      style: GoogleFonts.inter(
                          fontSize: 12, color: AppColors.textSecondary),
                    ),
                  ],
                ),
              ),

              // Balance & Tag
              Column(
                crossAxisAlignment: CrossAxisAlignment.end,
                children: [
                  Text(
                    '₹${NumberFormat('#,##,###').format(balance.abs())}',
                    style: GoogleFonts.inter(
                        fontSize: 18, fontWeight: FontWeight.w700, color: color),
                  ),
                  const SizedBox(height: 4),
                  Container(
                    padding:
                    const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: color.withValues(alpha: 0.12),
                      borderRadius: BorderRadius.circular(8),
                    ),
                    child: Text(
                      isAsset ? 'Asset' : 'Liability',
                      style: GoogleFonts.inter(
                          fontSize: 10, fontWeight: FontWeight.w500, color: color),
                    ),
                  ),
                ],
              )
            ],
          ),
        ),
      ),
    );
  }

  IconData _getAccountIcon(AccountType type) {
    switch (type) {
      case AccountType.bank:
        return Icons.account_balance;
      case AccountType.cash:
        return Icons.money;
      case AccountType.creditCard:
        return Icons.credit_card;
      case AccountType.loan:
        return Icons.request_quote;
      case AccountType.investment:
        return Icons.trending_up;
      default:
        return Icons.account_balance_wallet;
    }
  }

  // --------------------------------------------------------------------------
  // LOADING & ERROR UI
  // --------------------------------------------------------------------------

  Widget _buildLoadingList() {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      child: Column(
        children: List.generate(
          3,
              (_) => Container(
            margin: const EdgeInsets.only(bottom: 12),
            height: 70,
            decoration: BoxDecoration(
              color: AppColors.lightSurface,
              borderRadius: BorderRadius.circular(16),
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNetworthLoading() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16, vertical: 12),
      height: 160,
      decoration: BoxDecoration(
        borderRadius: BorderRadius.circular(24),
        gradient: LinearGradient(
          colors: [
            const Color(0xFF6C63FF).withOpacity(0.5),
            const Color(0xFF00B4DB).withOpacity(0.5),
          ],
        ),
      ),
    );
  }

  Widget _buildNetworthError() {
    return Padding(
      padding: const EdgeInsets.all(16),
      child: Text(
        "Failed to load net worth",
        style: TextStyle(color: AppColors.error),
      ),
    );
  }

  Widget _buildErrorList() {
    return const Padding(
      padding: EdgeInsets.all(40),
      child: Text("Failed to load accounts. Pull down to retry."),
    );
  }

  // --------------------------------------------------------------------------
  // BOTTOM SHEET
  // --------------------------------------------------------------------------

  void _showAccountTypeSelector() {
    showModalBottomSheet(
      context: context,
      shape:
      const RoundedRectangleBorder(borderRadius: BorderRadius.vertical(top: Radius.circular(20))),
      builder: (_) {
        return Padding(
          padding: const EdgeInsets.all(16.0),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              ListTile(
                leading: const Icon(Icons.account_balance_wallet, color: Colors.green),
                title: const Text("Add Asset Account"),
                onTap: () {
                  Navigator.pop(context);
                  _openAddAccountPage(AccountCategory.asset);
                },
              ),
              ListTile(
                leading: const Icon(Icons.money_off, color: Colors.red),
                title: const Text("Add Liability Account"),
                onTap: () {
                  Navigator.pop(context);
                  _openAddAccountPage(AccountCategory.liability);
                },
              )
            ],
          ),
        );
      },
    );
  }
}
