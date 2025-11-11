import 'package:finance_app/pages/add_account_page.dart';
import 'package:flutter/material.dart';
import 'package:intl/intl.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:finance_app/data/models/account.dart';
import 'package:finance_app/data/models/networth_summary.dart';
import 'package:finance_app/data/services/account_service.dart';
import 'package:finance_app/utils/app_style_constants.dart';

class AccountsPage extends StatefulWidget {
  const AccountsPage({super.key});

  @override
  State<AccountsPage> createState() => _AccountsPageState();
}

class _AccountsPageState extends State<AccountsPage> with TickerProviderStateMixin {
  late Future<List<Account>> _accountsFuture;
  late Future<NetworthSummary> _networthSummaryFuture;
  late AnimationController _fadeController;
  late AnimationController _slideController;

  @override
  void initState() {
    super.initState();
    var accountService = AccountService();
    _accountsFuture = accountService.getAccounts();
    _networthSummaryFuture = accountService.getNetWorth();

    // Initialize animations
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

  Future<void> _handleRefresh() async {
    setState(() {
      var accountService = AccountService();
      _accountsFuture = accountService.getAccounts();
      _networthSummaryFuture = accountService.getNetWorth();
    });
    await Future.delayed(const Duration(milliseconds: 500));
  }

  List<Account> accounts = [];

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: AppColors.lightBackground,
      body: CustomScrollView(
        slivers: [
          // Content
          SliverToBoxAdapter(
            child: FadeTransition(
              opacity: _fadeController,
              child: SlideTransition(
                position: Tween<Offset>(
                  begin: const Offset(0, 0.1),
                  end: Offset.zero,
                ).animate(_slideController),
                child: RefreshIndicator(
                  onRefresh: _handleRefresh,
                  child: Column(
                    children: [
                      FutureBuilder<NetworthSummary>(
                        future: _networthSummaryFuture,
                        builder: (context, snapshot) {
                          if (snapshot.connectionState == ConnectionState.waiting) {
                            return _buildNetWorthCardLoading();
                          } else if (snapshot.hasError) {
                            return _buildNetWorthCardError();
                          } else if (snapshot.hasData) {
                            return _buildNetWorthCard(snapshot.data!);
                          } else {
                            return _buildNetWorthCardLoading();
                          }
                        },
                      ),
                      const SizedBox(height: 16),
                      FutureBuilder<List<Account>>(
                        future: _accountsFuture,
                        builder: (context, snapshot) {
                          if (snapshot.connectionState == ConnectionState.waiting) {
                            return _buildLoadingState();
                          } else if (snapshot.hasError) {
                            return _buildErrorState();
                          } else if (!snapshot.hasData || snapshot.data!.isEmpty) {
                            return _buildEmptyState();
                          } else {
                            accounts = snapshot.data!;
                            return _buildAccountsList();
                          }
                        },
                      ),
                    ],
                  ),
                ),
              ),
            ),
          ),
        ],
      ),
      floatingActionButton: FloatingActionButton(
        onPressed: _showAddAccountDialog,
        backgroundColor: const Color(0xFF6C63FF),
        elevation: 8,
        child: const Icon(Icons.add, color: Colors.white),
      ),
    );
  }

  Widget _buildNetWorthCard(NetworthSummary summary) {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
      padding: const EdgeInsets.all(24.0),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Color(0xFF6C63FF), // Purple
            Color(0xFF00B4DB), // Teal Blue
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20.0),
        boxShadow: [
          BoxShadow(
            color: AppColors.primaryBlue.withValues(alpha: 0.15),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Top Row
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Total Net Worth',
                style: GoogleFonts.inter(
                  color: Color(0xFFF9FAFB),
                  fontSize: 14.0,
                  fontWeight: AppTypography.weightRegular,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(12.0),
                ),
                child: Text(
                  '${summary.assets.number + summary.liabilities.number} Accounts',
                  style: GoogleFonts.inter(
                    color: Color(0xFFF9FAFB),
                    fontSize: 12.0,
                    fontWeight: AppTypography.weightRegular,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12.0),

          // Main Amount - Net Worth from API
          Text(
            summary.formattedNetWorth,
            style: GoogleFonts.inter(
              color: Color(0xFFF9FAFB),
              fontSize: AppTypography.h1,
              fontWeight: AppTypography.weightBold,
            ),
          ),

          // Change indicator (placeholder)
          Row(
            children: [
              Icon(
                summary.isPositiveNetWorth ? Icons.trending_up : Icons.trending_down,
                color: summary.isPositiveNetWorth ? AppColors.success : AppColors.error,
                size: 16.0,
              ),
              const SizedBox(width: 4.0),
              Text(
                '↑ 0% from last month',
                style: GoogleFonts.inter(
                  color: Color(0xFFF9FAFB),
                  fontSize: 13.0,
                  fontWeight: AppTypography.weightRegular,
                ),
              ),
            ],
          ),

          const SizedBox(height: 20.0),

          // Bottom Row - Assets and Liabilities from API
          Row(
            children: [
              // Assets
              Expanded(
                child: Row(
                  children: [
                    Icon(
                      Icons.trending_up,
                      color: AppColors.success.withOpacity(0.8),
                      size: 20.0,
                    ),
                    const SizedBox(width: 8.0),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Assets',
                          style: GoogleFonts.inter(
                            color: Color(0xFFF9FAFB),
                            fontSize: 12.0,
                            fontWeight: AppTypography.weightRegular,
                          ),
                        ),
                        Text(
                          summary.formattedAssets,
                          style: GoogleFonts.inter(
                            color: Color(0xFFF9FAFB),
                            fontSize: 18.0,
                            fontWeight: AppTypography.weightSemibold,
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
              ),

              // Liabilities
              Expanded(
                child: Row(
                  children: [
                    Icon(
                      Icons.trending_down,
                      color: AppColors.error.withOpacity(0.8),
                      size: 20.0,
                    ),
                    const SizedBox(width: 8.0),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Liabilities',
                          style: GoogleFonts.inter(
                            color: Color(0xFFF9FAFB),
                            fontSize: 12.0,
                            fontWeight: AppTypography.weightRegular,
                          ),
                        ),
                        Text(
                          summary.formattedLiabilities,
                          style: GoogleFonts.inter(
                            color: Color(0xFFF9FAFB),
                            fontSize: 18.0,
                            fontWeight: AppTypography.weightSemibold,
                          ),
                        ),
                      ],
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

  Widget _buildNetWorthCardLoading() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
      padding: const EdgeInsets.all(24.0),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            Color(0xFF6C63FF).withOpacity(0.7),
            Color(0xFF00B4DB).withOpacity(0.7),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20.0),
        boxShadow: [
          BoxShadow(
            color: AppColors.primaryBlue.withValues(alpha: 0.1),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            mainAxisAlignment: MainAxisAlignment.spaceBetween,
            children: [
              Text(
                'Total Net Worth',
                style: GoogleFonts.inter(
                  color: Color(0xFFF9FAFB),
                  fontSize: 14.0,
                  fontWeight: AppTypography.weightRegular,
                ),
              ),
              Container(
                padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
                decoration: BoxDecoration(
                  color: Colors.white.withOpacity(0.2),
                  borderRadius: BorderRadius.circular(12.0),
                ),
                child: Text(
                  'Loading...',
                  style: GoogleFonts.inter(
                    color: Color(0xFFF9FAFB),
                    fontSize: 12.0,
                    fontWeight: AppTypography.weightRegular,
                  ),
                ),
              ),
            ],
          ),
          const SizedBox(height: 12.0),
          Container(
            width: 200,
            height: 40,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              borderRadius: BorderRadius.circular(8.0),
            ),
          ),
          const SizedBox(height: 8.0),
          Container(
            width: 120,
            height: 16,
            decoration: BoxDecoration(
              color: Colors.white.withOpacity(0.2),
              borderRadius: BorderRadius.circular(4.0),
            ),
          ),
          const SizedBox(height: 20.0),
          Row(
            children: [
              Expanded(
                child: Container(
                  height: 50,
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8.0),
                  ),
                ),
              ),
              const SizedBox(width: 16.0),
              Expanded(
                child: Container(
                  height: 50,
                  decoration: BoxDecoration(
                    color: Colors.white.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(8.0),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildNetWorthCardError() {
    return Container(
      margin: const EdgeInsets.symmetric(horizontal: 16.0, vertical: 12.0),
      padding: const EdgeInsets.all(24.0),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [
            AppColors.error.withOpacity(0.7),
            AppColors.error.withOpacity(0.5),
          ],
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
        ),
        borderRadius: BorderRadius.circular(20.0),
        boxShadow: [
          BoxShadow(
            color: AppColors.error.withValues(alpha: 0.1),
            blurRadius: 24,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.center,
        children: [
          Icon(
            Icons.error_outline,
            color: Colors.white,
            size: 48.0,
          ),
          const SizedBox(height: 12.0),
          Text(
            'Failed to load net worth',
            style: GoogleFonts.inter(
              color: Colors.white,
              fontSize: 16.0,
              fontWeight: AppTypography.weightSemibold,
            ),
          ),
          const SizedBox(height: 8.0),
          Text(
            'Using local calculation',
            style: GoogleFonts.inter(
              color: Colors.white.withOpacity(0.8),
              fontSize: 14.0,
            ),
          ),
          const SizedBox(height: 16.0),
          // Fallback to local calculation
          _buildNetWorthCardFallback(),
        ],
      ),
    );
  }

  Widget _buildNetWorthCardFallback() {
    final totalBalance = _calculateTotalBalance();
    final assetsTotal = _calculateAssetsTotal();
    final liabilitiesTotal = _calculateLiabilitiesTotal();

    return Column(
      children: [
        Text(
          '₹${NumberFormat('#,##,###').format(totalBalance)}',
          style: GoogleFonts.inter(
            color: Colors.white,
            fontSize: AppTypography.h1,
            fontWeight: AppTypography.weightBold,
          ),
        ),
        const SizedBox(height: 20.0),
        Row(
          children: [
            Expanded(
              child: Column(
                children: [
                  Text(
                    'Assets',
                    style: GoogleFonts.inter(
                      color: Colors.white,
                      fontSize: 12.0,
                    ),
                  ),
                  Text(
                    '₹${NumberFormat('#,##,###').format(assetsTotal)}',
                    style: GoogleFonts.inter(
                      color: Colors.white,
                      fontSize: 18.0,
                      fontWeight: AppTypography.weightSemibold,
                    ),
                  ),
                ],
              ),
            ),
            Expanded(
              child: Column(
                children: [
                  Text(
                    'Liabilities',
                    style: GoogleFonts.inter(
                      color: Colors.white,
                      fontSize: 12.0,
                    ),
                  ),
                  Text(
                    '₹${NumberFormat('#,##,###').format(liabilitiesTotal)}',
                    style: GoogleFonts.inter(
                      color: Colors.white,
                      fontSize: 18.0,
                      fontWeight: AppTypography.weightSemibold,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ],
    );
  }

  double _calculateTotalBalance() {
    return _calculateAssetsTotal() - _calculateLiabilitiesTotal();
  }

  double _calculateAssetsTotal() {
    return accounts
        .where((account) => account.isAsset)
        .map((account) => account.displayBalance)
        .fold(0.0, (sum, balance) => sum + balance);
  }

  double _calculateLiabilitiesTotal() {
    return accounts
        .where((account) => !account.isAsset)
        .map((account) => account.displayBalance.abs())
        .fold(0.0, (sum, balance) => sum + balance);
  }

  Widget _buildAccountsList() {
    final assetAccounts = accounts.where((account) => account.isAsset).toList();
    final liabilityAccounts = accounts.where((account) => !account.isAsset).toList();

    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      shrinkWrap: true,
      children: [
        if (assetAccounts.isNotEmpty) ...[
          _buildSectionHeader('Assets'),
          ...assetAccounts.map(_buildAccountCard),
        ],
        if (liabilityAccounts.isNotEmpty) ...[
          _buildSectionHeader('Liabilities'),
          ...liabilityAccounts.map(_buildAccountCard),
        ],
      ],
    );
  }

  double _getAccountBalance(Account account) {
    return account.displayBalance;
  }

  Widget _buildSectionHeader(String title) {
    return Container(
      margin: const EdgeInsets.only(top: 16.0, bottom: 8.0),
      child: Row(
        children: [
          Container(
            width: 3.0,
            height: 20.0,
            decoration: BoxDecoration(
              color: title == 'Assets' ? AppColors.success : AppColors.error,
              borderRadius: BorderRadius.circular(2.0),
            ),
          ),
          const SizedBox(width: 8.0),
          Text(
            title,
            style: GoogleFonts.inter(
              fontSize: AppTypography.h2,
              fontWeight: AppTypography.weightSemibold,
              color: AppColors.lightTextPrimary,
            ),
          ),
          const SizedBox(width: 8.0),
          Text(
            '(${title == 'Assets' ? accounts.where((account) => account.isAsset).length : accounts.where((account) => !account.isAsset).length} account${(title == 'Assets' ? accounts.where((account) => account.isAsset).length : accounts.where((account) => !account.isAsset).length) != 1 ? 's' : ''})',
            style: GoogleFonts.inter(
              fontSize: 13.0,
              fontWeight: AppTypography.weightRegular,
              color: AppColors.textSecondary,
            ),
          ),
          const Spacer(),
          TextButton(
            onPressed: () {},
            style: TextButton.styleFrom(
              padding: EdgeInsets.zero,
              minimumSize: const Size(0, 0),
              tapTargetSize: MaterialTapTargetSize.shrinkWrap,
            ),
            child: Row(
              children: [
                Text(
                  'View All',
                  style: GoogleFonts.inter(
                    fontSize: 14.0,
                    fontWeight: AppTypography.weightRegular,
                    color: AppColors.primaryBlue,
                  ),
                ),
                Icon(
                  Icons.arrow_forward_ios,
                  size: 12.0,
                  color: AppColors.primaryBlue,
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAccountCard(Account account) {
    final balance = _getAccountBalance(account);
    final isAsset = account.isAsset;
    final accountColor = _getAccountColor(account);

    return Container(
      margin: const EdgeInsets.only(bottom: 12.0),
      decoration: BoxDecoration(
        color: AppColors.lightSurface,
        borderRadius: BorderRadius.circular(AppBorderRadius.large),
        border: Border.all(color: AppColors.cardBorder),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withOpacity(0.04),
            blurRadius: 8,
            offset: const Offset(0, 2),
          ),
        ],
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          borderRadius: BorderRadius.circular(AppBorderRadius.large),
          onTap: () => _showAccountDetails(account),
          child: Padding(
            padding: const EdgeInsets.all(16.0),
            child: Row(
              children: [
                // Account Icon
                Container(
                  width: 48.0,
                  height: 48.0,
                  padding: const EdgeInsets.all(12.0),
                  decoration: BoxDecoration(
                    color: accountColor.withOpacity(0.1),
                    borderRadius: BorderRadius.circular(AppBorderRadius.medium),
                  ),
                  child: Icon(
                    _getAccountIcon(account),
                    color: accountColor,
                    size: 24.0,
                  ),
                ),
                const SizedBox(width: 16.0),

                // Account Details
                Expanded(
                  child: Column(
                    crossAxisAlignment: CrossAxisAlignment.start,
                    children: [
                      Text(
                        account.accountName,
                        style: GoogleFonts.inter(
                          fontWeight: AppTypography.weightSemibold,
                          fontSize: AppTypography.h3,
                          color: AppColors.lightTextPrimary,
                        ),
                      ),
                      const SizedBox(height: 4.0),
                      Text(
                        account.displayType.toUpperCase(),
                        style: GoogleFonts.inter(
                          fontSize: AppTypography.caption,
                          fontWeight: AppTypography.weightRegular,
                          color: AppColors.textSecondary,
                          letterSpacing: 0.5,
                        ),
                      ),
                    ],
                  ),
                ),

                // Balance and Type Badge
                Column(
                  crossAxisAlignment: CrossAxisAlignment.end,
                  children: [
                    Text(
                      '₹${NumberFormat('#,##,###').format(balance.abs())}',
                      style: GoogleFonts.inter(
                        fontWeight: AppTypography.weightSemibold,
                        fontSize: 20.0,
                        color: isAsset ? AppColors.success : AppColors.error,
                      ),
                    ),
                    const SizedBox(height: 4.0),
                    Container(
                      padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
                      decoration: BoxDecoration(
                        color: (isAsset ? AppColors.success : AppColors.error).withOpacity(0.1),
                        borderRadius: BorderRadius.circular(AppBorderRadius.small),
                      ),
                      child: Text(
                        isAsset ? 'Asset' : 'Liability',
                        style: GoogleFonts.inter(
                          fontSize: 10.0,
                          fontWeight: AppTypography.weightMedium,
                          color: isAsset ? AppColors.success : AppColors.error,
                        ),
                      ),
                    ),
                  ],
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  IconData _getAccountIcon(Account account) {
    final name = account.accountName.toLowerCase();
    if (name.contains('bank') || name.contains('savings')) {
      return Icons.account_balance;
    } else if (name.contains('cash')) {
      return Icons.money;
    } else if (name.contains('wallet') || name.contains('paytm') || name.contains('gpay')) {
      return Icons.account_balance_wallet;
    } else if (name.contains('credit')) {
      return Icons.credit_card;
    } else if (name.contains('investment')) {
      return Icons.trending_up;
    } else if (name.contains('loan')) {
      return Icons.assignment;
    }
    return Icons.account_balance;
  }

  Color _getAccountColor(Account account) {
    final name = account.accountName.toLowerCase();
    if (name.contains('bank') || name.contains('savings')) {
      return AppColors.primaryBlue; // Blue
    } else if (name.contains('cash')) {
      return AppColors.success; // Green
    } else if (name.contains('wallet') || name.contains('paytm') || name.contains('gpay')) {
      return AppColors.primaryGradientEnd; // Purple
    } else if (name.contains('credit')) {
      return AppColors.warning; // Orange
    } else if (name.contains('investment')) {
      return AppColors.secondaryTeal; // Teal
    } else if (name.contains('loan')) {
      return AppColors.error; // Red
    }
    return AppColors.primaryBlue; // Default color
  }

  // Enhanced animations and micro-interactions
  void _onAccountCardTap(Account account) {
    // Add haptic feedback and smooth navigation
    // HapticFeedback.lightImpact();
    _showAccountDetails(account);
  }



  void _showAccountDetails(Account account) {
    showModalBottomSheet(
      context: context,
      shape: const RoundedRectangleBorder(
        borderRadius: BorderRadius.vertical(top: Radius.circular(20)),
      ),
      builder: (context) => _buildAccountDetailsSheet(account),
    );
  }

  Widget _buildAccountDetailsSheet(Account account) {
    final balance = _getAccountBalance(account);
    final isAsset = account.isAsset;
    final accountColor = _getAccountColor(account);

    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 20.0, vertical: 16.0),
      decoration: BoxDecoration(
        color: AppColors.lightSurface.withValues(alpha: 0.95),
        borderRadius: const BorderRadius.vertical(top: Radius.circular(24.0)),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.1),
            blurRadius: 10,
            offset: const Offset(0, -2),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // ─── Drag Handle ───────────────────────────────────────────
          Center(
            child: Container(
              width: 40,
              height: 4,
              margin: const EdgeInsets.only(bottom: 16),
              decoration: BoxDecoration(
                color: AppColors.textSecondary.withValues(alpha: 0.3),
                borderRadius: BorderRadius.circular(20),
              ),
            ),
          ),

          // ─── Header ───────────────────────────────────────────────
          Row(
            children: [
              Container(
                width: 48.0,
                height: 48.0,
                padding: const EdgeInsets.all(12.0),
                decoration: BoxDecoration(
                  color: accountColor.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(AppBorderRadius.medium),
                ),
                child: Icon(
                  _getAccountIcon(account),
                  color: accountColor,
                  size: 24.0,
                ),
              ),
              const SizedBox(width: 12.0),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      account.accountName,
                      style: GoogleFonts.inter(
                        fontWeight: AppTypography.weightSemibold,
                        fontSize: 18.0,
                        color: AppColors.lightTextPrimary,
                      ),
                    ),
                    Text(
                      account.displayType,
                      style: GoogleFonts.inter(
                        fontSize: AppTypography.body,
                        color: AppColors.textSecondary,
                      ),
                    ),
                  ],
                ),
              ),
            ],
          ),

          const SizedBox(height: 20.0),

          // ─── Balance Card ───────────────────────────────────────────
          Container(
            padding: const EdgeInsets.all(16.0),
            decoration: BoxDecoration(
              gradient: LinearGradient(
                colors: isAsset
                    ? [AppColors.success.withValues(alpha: 0.1), AppColors.success.withValues(alpha: 0.05)]
                    : [AppColors.error.withValues(alpha: 0.1), AppColors.error.withValues(alpha: 0.05)],
                begin: Alignment.topLeft,
                end: Alignment.bottomRight,
              ),
              borderRadius: BorderRadius.circular(AppBorderRadius.medium),
              border: Border.all(
                color: (isAsset ? AppColors.success : AppColors.error).withValues(alpha: 0.2),
              ),
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Text(
                  'Current Balance',
                  style: GoogleFonts.inter(
                    fontSize: AppTypography.body,
                    color: AppColors.textSecondary,
                  ),
                ),
                TweenAnimationBuilder<double>(
                  tween: Tween(begin: 0, end: balance.toDouble()),
                  duration: const Duration(milliseconds: 800),
                  builder: (context, value, _) {
                    return Text(
                      '₹${NumberFormat('#,##,###').format(value)}',
                      style: GoogleFonts.inter(
                        fontSize: 18.0,
                        fontWeight: AppTypography.weightSemibold,
                        color: isAsset ? AppColors.success : AppColors.error,
                      ),
                    );
                  },
                ),
              ],
            ),
          ),

          const SizedBox(height: 16.0),

          // Optional insight / last transaction
          Text(
            'Last transaction: +₹500 on Nov 10',
            style: GoogleFonts.inter(
              fontSize: 13.0,
              color: AppColors.textSecondary.withValues(alpha: 0.7),
            ),
          ),

          const SizedBox(height: 24.0),

          // ─── Action Buttons ─────────────────────────────────────────
          Row(
            children: [
              Expanded(
                child: ElevatedButton(
                  onPressed: () {},
                  style: ElevatedButton.styleFrom(
                    backgroundColor: AppColors.primaryBlue,
                    foregroundColor: Colors.white,
                    padding: const EdgeInsets.symmetric(vertical: 12.0),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(AppBorderRadius.medium),
                    ),
                  ),
                  child: Text(
                    'View Transactions',
                    style: GoogleFonts.inter(
                      fontSize: AppTypography.body,
                      fontWeight: AppTypography.weightMedium,
                    ),
                  ),
                ),
              ),
              const SizedBox(width: 12.0),
              Expanded(
                child: OutlinedButton(
                  onPressed: () {},
                  style: OutlinedButton.styleFrom(
                    padding: const EdgeInsets.symmetric(vertical: 12.0),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(AppBorderRadius.medium),
                    ),
                    side: BorderSide(color: AppColors.primaryBlue),
                  ),
                  child: Text(
                    'Edit Account',
                    style: GoogleFonts.inter(
                      fontSize: AppTypography.body,
                      fontWeight: AppTypography.weightMedium,
                      color: AppColors.primaryBlue,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ],
      ),
    );
  }


  Widget _buildLoadingState() {
    return ListView.builder(
      padding: const EdgeInsets.symmetric(horizontal: 16.0),
      itemCount: 3,
      shrinkWrap: true,
      itemBuilder: (context, index) {
        return Container(
          margin: const EdgeInsets.only(bottom: 12.0),
          padding: const EdgeInsets.all(16.0),
          decoration: BoxDecoration(
            color: AppColors.lightSurface,
            borderRadius: BorderRadius.circular(AppBorderRadius.large),
            border: Border.all(color: AppColors.cardBorder),
          ),
          child: Row(
            children: [
              Container(
                width: 48.0,
                height: 48.0,
                decoration: BoxDecoration(
                  color: AppColors.textSecondary.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(AppBorderRadius.medium),
                ),
              ),
              const SizedBox(width: 16.0),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Container(
                      width: 120.0,
                      height: 18.0,
                      decoration: BoxDecoration(
                        color: AppColors.textSecondary.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(4.0),
                      ),
                    ),
                    const SizedBox(height: 6.0),
                    Container(
                      width: 80.0,
                      height: 14.0,
                      decoration: BoxDecoration(
                        color: AppColors.textSecondary.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(4.0),
                      ),
                    ),
                  ],
                ),
              ),
              Container(
                width: 80.0,
                height: 18.0,
                decoration: BoxDecoration(
                  color: AppColors.textSecondary.withValues(alpha: 0.1),
                  borderRadius: BorderRadius.circular(4.0),
                ),
              ),
            ],
          ),
        );
      },
    );
  }

  Widget _buildErrorState() {
    return Center(
      child: Column(
        mainAxisAlignment: MainAxisAlignment.center,
        children: [
          Container(
            padding: const EdgeInsets.all(16.0),
            decoration: BoxDecoration(
              color: AppColors.error.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(50.0),
            ),
            child: Icon(
              Icons.error_outline,
              color: AppColors.error,
              size: 64.0,
            ),
          ),
          const SizedBox(height: 16.0),
          Text(
            'Failed to load accounts',
            style: GoogleFonts.inter(
              color: AppColors.error,
              fontSize: 18.0,
              fontWeight: AppTypography.weightSemibold,
            ),
          ),
          const SizedBox(height: 8.0),
          Text(
            'Please check your connection and try again',
            style: GoogleFonts.inter(
              color: AppColors.textSecondary,
              fontSize: AppTypography.body,
            ),
          ),
          const SizedBox(height: 24.0),
          ElevatedButton(
            onPressed: _handleRefresh,
            style: ElevatedButton.styleFrom(
              backgroundColor: AppColors.primaryBlue,
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 32.0, vertical: 12.0),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(AppBorderRadius.medium),
              ),
            ),
            child: Text(
              'Retry',
              style: GoogleFonts.inter(
                fontSize: AppTypography.body,
                fontWeight: AppTypography.weightMedium,
              ),
            ),
          ),
        ],
      ),
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
              color: AppColors.textSecondary.withOpacity(0.1),
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
            onPressed: _showAddAccountDialog,
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

  void _showAddAccountDialog() {
    Navigator.push(
      context,
      MaterialPageRoute(builder: (context) => const AddAccountPage()),
    );
  }
}
