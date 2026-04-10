import 'package:finance_app/features/account/application/accounts_controller.dart';
import 'package:finance_app/features/account/data/model/account.dart';
import 'package:finance_app/features/category/data/models/category.dart';
import 'package:finance_app/features/category/application/category_controller.dart';
import 'package:finance_app/features/sms/application/sms_controller.dart';
import 'package:finance_app/features/sms/data/model/transaction_draft.dart';
import 'package:finance_app/features/transaction/data/model/transaction.dart';

import 'package:finance_app/features/transaction/ui/transaction_form_page.dart';
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:intl/intl.dart';
import 'package:permission_handler/permission_handler.dart';

class SmsReviewPage extends ConsumerStatefulWidget {
  const SmsReviewPage({super.key});

  @override
  ConsumerState<SmsReviewPage> createState() => _SmsReviewPageState();
}

class _SmsReviewPageState extends ConsumerState<SmsReviewPage>
    with SingleTickerProviderStateMixin {
  late AnimationController _controller;
  late Animation<double> _animation;

  @override
  void initState() {
    super.initState();
    _requestSmsPermission();
    _controller = AnimationController(
      duration: const Duration(milliseconds: 300),
      vsync: this,
    );
    _animation = CurvedAnimation(parent: _controller, curve: Curves.easeInOut);
  }

  @override
  void dispose() {
    _controller.dispose();
    super.dispose();
  }

  Future<void> _requestSmsPermission() async {
    await Permission.sms.request();
  }

  Future<void> _editDraft(TransactionDraft draft) async {
    // Convert draft to Transaction for initial data
    final transaction = Transaction(
      transactionName: draft.transactionName,
      amount: draft.amount,
      type: draft.type,
      // Pass empty strings if null, checking logic in FormPage will handle "Select Account"
      accountId: draft.accountId ?? '',
      categoryId: draft.categoryId ?? '',
      occurredAt: draft.occurredAt,
      currency: draft.currency,
      notes: draft.notes, // don't use original message as notes
    );

    final result = await Navigator.push(
      context,
      MaterialPageRoute(
        builder: (context) => TransactionFormPage(
          initialData: transaction,
          isDraft: true,
          originalMessage: draft.originalMessage,
        ),
      ),
    );

    if (result is Transaction) {
      // If transaction returned, update the draft
      ref.read(smsControllerProvider.notifier).updateDraft(draft.id, result);

      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(const SnackBar(content: Text('Draft updated')));
      }
    }
  }

  Future<void> _confirmDrafts(List<String> ids) async {
    final draftsState = ref.read(smsControllerProvider);
    if (!draftsState.hasValue) return;

    final allDrafts = draftsState.value!;
    final draftsToConfirm = allDrafts.where((d) => ids.contains(d.id)).toList();

    // specific validation: check if any selected draft is missing account or category
    final invalidDrafts = draftsToConfirm.where((draft) {
      return (draft.accountId == null || draft.accountId!.isEmpty) ||
          (draft.categoryId == null || draft.categoryId!.isEmpty);
    }).toList();

    if (invalidDrafts.isNotEmpty) {
      if (mounted) {
        showDialog(
          context: context,
          builder: (context) => AlertDialog(
            title: const Text('Missing Details'),
            content: Text(
              'Please select an Account and Category for ${invalidDrafts.length} transaction(s) before confirming.',
            ),
            actions: [
              TextButton(
                onPressed: () => Navigator.pop(context),
                child: const Text('OK'),
              ),
            ],
          ),
        );
      }
      return;
    }

    await ref.read(smsControllerProvider.notifier).confirmDrafts(ids);
    if (mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(content: Text('${ids.length} Draft(s) confirmed')),
      );
    }
  }

  @override
  Widget build(BuildContext context) {
    final draftsState = ref.watch(smsControllerProvider);

    // Filter checked drafts to determine if FAB should show
    final checkedDrafts =
        draftsState.asData?.value.where((d) => d.isChecked).toList() ?? [];
    if (checkedDrafts.isNotEmpty) {
      _controller.forward();
    } else {
      _controller.reverse();
    }

    return Scaffold(
      backgroundColor: const Color(0xFFF8FAFC), // background-light from design
      body: Stack(
        children: [
          Column(
            children: [
              _buildHeader(draftsState.asData?.value.length ?? 0),
              Expanded(
                child: draftsState.when(
                  data: (drafts) {
                    if (drafts.isEmpty) {
                      return _buildEmptyState();
                    }
                    return ListView.builder(
                      padding: const EdgeInsets.fromLTRB(16, 0, 16, 140),
                      itemCount:
                          drafts.length + 2, // +2 for Scan Card and Header
                      itemBuilder: (context, index) {
                        if (index == 0) return _buildScanCard();
                        if (index == 1) return _buildDraftsListHeader(drafts);
                        final draft = drafts[index - 2];
                        return _buildDraftCard(draft);
                      },
                    );
                  },
                  error: (err, stack) => Center(child: Text('Error: $err')),
                  loading: () =>
                      const Center(child: CircularProgressIndicator()),
                ),
              ),
            ],
          ),
          // Floating Selection Bar
          Positioned(
            bottom: 32,
            left: 24,
            right: 24,
            child: ScaleTransition(
              scale: _animation,
              child: _buildFloatingSelectionBar(checkedDrafts),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return ListView(
      padding: const EdgeInsets.symmetric(horizontal: 16),
      children: [
        _buildScanCard(),
        const SizedBox(height: 60),
        Center(
          child: Column(
            children: [
              Icon(Icons.inbox_rounded, size: 60, color: Colors.grey[300]),
              const SizedBox(height: 16),
              Text(
                "No SMS drafts found",
                style: GoogleFonts.plusJakartaSans(
                  color: Colors.grey[400],
                  fontSize: 16,
                  fontWeight: FontWeight.w600,
                ),
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildHeader(int count) {
    return Container(
      padding: const EdgeInsets.only(
        left: 24,
        right: 24,
        top: 20 + 32,
        bottom: 20,
      ), // Added top padding for status bar
      decoration: BoxDecoration(
        color: Colors.white.withValues(alpha: 0.9),
        border: Border(bottom: BorderSide(color: Colors.grey[100]!)),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                'Review Drafts',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 24,
                  fontWeight: FontWeight.w800,
                  color: const Color(0xFF0F172A),
                  letterSpacing: -0.5,
                ),
              ),
              const SizedBox(height: 4),
              Row(
                children: [
                  Container(
                    width: 8,
                    height: 8,
                    decoration: const BoxDecoration(
                      color: Color(0xFF10B981), // emerald-500
                      shape: BoxShape.circle,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Text(
                    '$count Transactions Found',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      fontWeight: FontWeight.w700,
                      color: const Color(0xFF94A3B8), // slate-400
                      letterSpacing: 0.5,
                    ),
                  ),
                ],
              ),
            ],
          ),
          Row(
            children: [
              Stack(
                clipBehavior: Clip.none,
                children: [
                  _buildHeaderIcon(Icons.notifications_outlined),
                  Positioned(
                    top: -4,
                    right: -4,
                    child: Container(
                      width: 16,
                      height: 16,
                      decoration: BoxDecoration(
                        color: const Color(0xFFF43F5E), // rose-500
                        shape: BoxShape.circle,
                        border: Border.all(color: Colors.white, width: 2),
                      ),
                      child: Center(
                        child: Text(
                          '3',
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 8,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                          ),
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildHeaderIcon(IconData icon) {
    return Container(
      width: 40,
      height: 40,
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC), // slate-50
        borderRadius: BorderRadius.circular(16),
      ),
      child: Icon(
        icon,
        color: const Color(0xFF475569), // slate-600
        size: 20,
      ),
    );
  }

  Widget _buildScanCard() {
    return Container(
      margin: const EdgeInsets.symmetric(vertical: 24),
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A), // slate-900
        borderRadius: BorderRadius.circular(40), // rounded-[2.5rem]
        boxShadow: [
          BoxShadow(
            color: const Color(0xFFE2E8F0), // slate-200
            blurRadius: 25,
            offset: const Offset(0, 10),
          ),
        ],
      ),
      child: Stack(
        children: [
          Padding(
            padding: const EdgeInsets.all(24),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    Container(
                      width: 56,
                      height: 56,
                      decoration: BoxDecoration(
                        color: Colors.white.withValues(alpha: 0.1),
                        borderRadius: BorderRadius.circular(16),
                        border: Border.all(
                          color: Colors.white.withValues(alpha: 0.1),
                        ),
                      ),
                      child: const Icon(
                        Icons.bolt,
                        color: Color(0xFFFACC15), // yellow-400
                        size: 28,
                      ),
                    ),
                    const SizedBox(width: 16),
                    Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          'Sync History',
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 18,
                            fontWeight: FontWeight.bold,
                            color: Colors.white,
                            height: 1.1,
                          ),
                        ),
                        const SizedBox(height: 4),
                        Text(
                          'Scan last 48h of SMS messages',
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 12,
                            color: const Color(0xFF94A3B8), // slate-400
                          ),
                        ),
                      ],
                    ),
                  ],
                ),
                Container(
                  width: 40,
                  height: 40,
                  decoration: BoxDecoration(
                    color: Colors.white.withValues(alpha: 0.1),
                    borderRadius: BorderRadius.circular(12),
                    border: Border.all(
                      color: Colors.white.withValues(alpha: 0.05),
                    ),
                  ),
                  child: const Icon(
                    Icons.chevron_right,
                    color: Colors.white,
                    size: 16,
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDraftsListHeader(List<TransactionDraft> drafts) {
    return Padding(
      padding: const EdgeInsets.symmetric(horizontal: 8, vertical: 8),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            'PENDING REVIEW',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w900,
              color: const Color(0xFF94A3B8), // slate-400
              letterSpacing: 1.5, // tracking-widest
            ),
          ),
          InkWell(
            onTap: () {
              ref.read(smsControllerProvider.notifier).selectAll();
            },
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 6),
              decoration: BoxDecoration(
                color: const Color(0xFFEEF2FF), // indigo-50
                borderRadius: BorderRadius.circular(20),
              ),
              child: Text(
                'Select All',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 12,
                  fontWeight: FontWeight.bold,
                  color: const Color(0xFF4F46E5), // indigo-600
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDraftCard(TransactionDraft draft) {
    final isSelected = draft.isChecked;
    final isIncome = draft.type == 'INCOME';

    final themeColor = isIncome
        ? const Color(0xFF10B981)
        : const Color(0xFF4F46E5);
    final themeBg = isIncome
        ? const Color(0xFFECFDF5)
        : const Color(0xFFEEF2FF);
    final themeBorder = isIncome
        ? const Color(0xFFA7F3D0)
        : const Color(0xFFE0E7FF);
    final themeAmount = isIncome
        ? const Color(0xFF10B981)
        : const Color(0xFFF43F5E);
    final themeEvidenceText = isIncome
        ? const Color(0xFF34D399)
        : const Color(0xFF818CF8);

    final accountsState = ref.watch(accountsControllerProvider);
    final categoriesState = ref.watch(childrenCategoriesProvider);

    final String accountName =
        accountsState.asData?.value
            .cast<Account?>()
            .firstWhere((a) => a?.id == draft.accountId, orElse: () => null)
            ?.accountName ??
        (draft.accountId != null && draft.accountId!.isNotEmpty
            ? draft.accountId!
            : 'Account');

    final String categoryName =
        categoriesState.asData?.value
            .cast<Category?>()
            .firstWhere((c) => c?.id == draft.categoryId, orElse: () => null)
            ?.name ??
        (draft.categoryId != null && draft.categoryId!.isNotEmpty
            ? draft.categoryId!
            : 'Category');

    return Dismissible(
      key: Key(draft.id),
      direction: DismissDirection.endToStart,
      onDismissed: (direction) {
        ref.read(smsControllerProvider.notifier).removeDraft(draft.id);
        ScaffoldMessenger.of(context).showSnackBar(
          SnackBar(
            content: Text('${draft.transactionName} removed'),
            action: SnackBarAction(label: 'Undo', onPressed: () {}),
          ),
        );
      },
      background: Container(
        margin: const EdgeInsets.only(bottom: 16),
        decoration: BoxDecoration(
          color: Colors.redAccent,
          borderRadius: BorderRadius.circular(40),
        ),
        alignment: Alignment.centerRight,
        padding: const EdgeInsets.only(right: 32),
        child: const Icon(Icons.delete_outline, color: Colors.white, size: 32),
      ),
      child: GestureDetector(
        onTap: () {
          ref
              .read(smsControllerProvider.notifier)
              .toggleDraft(draft.id, !isSelected);
        },
        child: Container(
          margin: const EdgeInsets.only(bottom: 16),
          padding: const EdgeInsets.all(24),
          decoration: BoxDecoration(
            color: isSelected ? const Color(0xFFF8FAFC) : Colors.white,
            borderRadius: BorderRadius.circular(40), // 2.5rem
            border: Border.all(
              color: isSelected
                  ? themeColor
                  : const Color(0xFFF1F5F9), // slate-100
              width: isSelected ? 2 : 1,
            ),
            boxShadow: isSelected
                ? [
                    BoxShadow(
                      color: themeColor.withValues(alpha: 0.1),
                      blurRadius: 20,
                      offset: const Offset(0, 8),
                    ),
                  ]
                : [
                    BoxShadow(
                      color: Colors.black.withValues(alpha: 0.02),
                      blurRadius: 4,
                      offset: const Offset(0, 1),
                    ),
                  ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              // Top Row
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisAlignment: MainAxisAlignment.spaceBetween,
                children: [
                  Expanded(
                    child: Row(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        // Icon
                        Stack(
                          clipBehavior: Clip.none,
                          children: [
                            Container(
                              width: 56, // w-14
                              height: 56, // h-14
                              decoration: BoxDecoration(
                                color: themeBg,
                                borderRadius: BorderRadius.circular(
                                  16,
                                ), // rounded-2xl
                                border: Border.all(color: themeBorder),
                              ),
                              child: Icon(
                                draft.categoryIcon ??
                                    (isIncome
                                        ? Icons.account_balance_wallet
                                        : Icons.directions_car),
                                color: themeColor,
                                size: 28,
                              ),
                            ),
                            if (isSelected)
                              Positioned(
                                top: -6,
                                right: -6,
                                child: Container(
                                  width: 20,
                                  height: 20,
                                  decoration: BoxDecoration(
                                    color: themeColor,
                                    shape: BoxShape.circle,
                                    border: Border.all(
                                      color: Colors.white,
                                      width: 2,
                                    ),
                                  ),
                                  child: const Icon(
                                    Icons.check,
                                    color: Colors.white,
                                    size: 12,
                                  ),
                                ),
                              ),
                          ],
                        ),
                        const SizedBox(width: 16), // gap-4
                        Expanded(
                          child: Column(
                            crossAxisAlignment: CrossAxisAlignment.start,
                            children: [
                              Row(
                                children: [
                                  Expanded(
                                    child: Text(
                                      draft.transactionName,
                                      style: GoogleFonts.plusJakartaSans(
                                        fontSize: 16, // Reduced from 20
                                        fontWeight: FontWeight.w800,
                                        color: const Color(0xFF1E293B),
                                        height: 1.2,
                                      ),
                                    ),
                                  ),
                                ],
                              ),
                              const SizedBox(height: 4),
                              Row(
                                children: [
                                  const Icon(
                                    Icons.access_time,
                                    size: 14,
                                    color: Color(0xFF94A3B8), // text-slate-400
                                  ),
                                  const SizedBox(width: 6), // gap-1.5
                                  Text(
                                    DateFormat(
                                      'MMM d • h:mm a',
                                    ).format(draft.occurredAt),
                                    style: GoogleFonts.plusJakartaSans(
                                      fontSize: 12, // Reduced from 14
                                      fontWeight:
                                          FontWeight.w500, // font-medium
                                      color: const Color(
                                        0xFF94A3B8,
                                      ), // text-slate-400
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
                  // Amount
                  Column(
                    crossAxisAlignment: CrossAxisAlignment.end,
                    children: [
                      Text(
                        '${isIncome ? '+' : '-'}₹${NumberFormat('#,##,##0.00').format(draft.amount)}',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 20, // Reduced from 24
                          fontWeight: FontWeight.w900,
                          color: themeAmount,
                          letterSpacing: -0.5,
                        ),
                      ),
                    ],
                  ),
                ],
              ),
              const SizedBox(height: 24), // space-y-6 roughly 24px
              // Selection Pills
              Row(
                children: [
                  Expanded(
                    child: _buildSelectionPill(
                      icon: Icons.grid_view_rounded,
                      label: categoryName,
                    ),
                  ),
                  const SizedBox(width: 8),
                  Expanded(
                    child: _buildSelectionPill(
                      icon: Icons.account_balance_wallet_outlined,
                      label: accountName,
                    ),
                  ),
                ],
              ),
              const SizedBox(height: 24),

              // Evidence
              Container(
                width: double.infinity,
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: themeBg.withValues(alpha: 0.5),
                  borderRadius: BorderRadius.circular(16),
                  border: Border.all(color: themeBorder.withValues(alpha: 0.5)),
                ),
                child: Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Icon(
                      Icons.info_outline,
                      size: 16,
                      color: themeEvidenceText,
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Text(
                        '"${draft.originalMessage}"',
                        style: GoogleFonts.plusJakartaSans(
                          fontSize: 11, // Reduced from 13
                          fontStyle: FontStyle.italic,
                          fontWeight: FontWeight.w600,
                          color: themeEvidenceText,
                          height: 1.4,
                        ),
                      ),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 16), // pt-2 space // space-y-6
              // Actions
              Row(
                children: [
                  Expanded(
                    flex: 1,
                    child: InkWell(
                      onTap: () => _editDraft(draft),
                      borderRadius: BorderRadius.circular(16),
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          vertical: 16,
                        ), // py-4
                        decoration: BoxDecoration(
                          color: const Color(0xFFF8FAFC), // slate-50
                          border: Border.all(
                            color: const Color(0xFFE2E8F0),
                          ), // slate-200
                          borderRadius: BorderRadius.circular(16), // 2xl
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Icon(
                              Icons.edit_outlined,
                              size: 18,
                              color: Color(0xFF475569),
                            ),
                            const SizedBox(width: 8),
                            Text(
                              'Edit',
                              style: GoogleFonts.plusJakartaSans(
                                fontSize: 14,
                                fontWeight: FontWeight.bold,
                                color: const Color(0xFF475569), // slate-600
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                  const SizedBox(width: 12),
                  Expanded(
                    flex: 2,
                    child: InkWell(
                      onTap: () => _confirmDrafts([draft.id]),
                      borderRadius: BorderRadius.circular(16),
                      child: Container(
                        padding: const EdgeInsets.symmetric(
                          vertical: 16,
                        ), // py-4
                        decoration: BoxDecoration(
                          color: themeColor,
                          borderRadius: BorderRadius.circular(16), // 2xl
                          boxShadow: [
                            BoxShadow(
                              color: themeColor.withValues(
                                alpha: 0.3,
                              ), // shadow-lg shadow-indigo-200 // theme bg
                              blurRadius: 15,
                              offset: const Offset(0, 4),
                            ),
                          ],
                        ),
                        child: Row(
                          mainAxisAlignment: MainAxisAlignment.center,
                          children: [
                            const Icon(
                              Icons.check,
                              size: 22,
                              color: Colors.white,
                            ),
                            const SizedBox(width: 8),
                            Text(
                              isIncome ? 'Confirm Income' : 'Confirm Expense',
                              style: GoogleFonts.plusJakartaSans(
                                fontSize: 14,
                                fontWeight: FontWeight.w800, // extrabold
                                color: Colors.white,
                              ),
                            ),
                          ],
                        ),
                      ),
                    ),
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }

  Widget _buildSelectionPill({required IconData icon, required String label}) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      decoration: BoxDecoration(
        color: const Color(0xFFF8FAFC),
        border: Border.all(color: const Color(0xFFF1F5F9)),
        borderRadius: BorderRadius.circular(12),
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Expanded(
            child: Row(
              children: [
                Icon(icon, size: 14, color: const Color(0xFF94A3B8)),
                const SizedBox(width: 6),
                Expanded(
                  child: Text(
                    label,
                    overflow: TextOverflow.ellipsis,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      fontWeight: FontWeight.bold,
                      color: const Color(0xFF475569),
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

  Widget _buildFloatingSelectionBar(List<TransactionDraft> checkedDrafts) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        color: const Color(0xFF4F46E5), // indigo-600
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF4F46E5).withValues(alpha: 0.4),
            blurRadius: 25,
            offset: const Offset(0, 10),
          ),
        ],
        border: Border.all(
          color: const Color(0xFF818CF8),
          width: 2,
        ), // indigo-400
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                width: 40,
                height: 40,
                decoration: BoxDecoration(
                  color: Colors.white.withValues(alpha: 0.2),
                  borderRadius: BorderRadius.circular(12),
                ),
                child: const Icon(
                  Icons.playlist_add_check,
                  color: Colors.white,
                  size: 20,
                ),
              ),
              const SizedBox(width: 12),
              Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    'Review ${checkedDrafts.length} Draft${checkedDrafts.length > 1 ? 's' : ''}',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 14,
                      fontWeight: FontWeight.bold,
                      color: Colors.white,
                    ),
                  ),
                  Text(
                    'Tap to add to your ledger',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 10,
                      color: const Color(0xFFC7D2FE), // indigo-200
                    ),
                  ),
                ],
              ),
            ],
          ),
          InkWell(
            onTap: () {
              final ids = checkedDrafts.map((d) => d.id).toList();
              _confirmDrafts(ids);
            },
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 20, vertical: 10),
              decoration: BoxDecoration(
                color: Colors.white,
                borderRadius: BorderRadius.circular(16),
                boxShadow: const [
                  BoxShadow(
                    color: Colors.black12,
                    blurRadius: 4,
                    offset: Offset(0, 2),
                  ),
                ],
              ),
              child: Text(
                'CONFIRM ALL',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 10,
                  fontWeight: FontWeight.w900,
                  color: const Color(0xFF4F46E5), // indigo-600
                  letterSpacing: 1.0,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }
}
