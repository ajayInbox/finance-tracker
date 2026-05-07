import 'package:finance_app/features/account/application/accounts_controller.dart';
import 'package:finance_app/features/category/application/category_controller.dart';
import 'package:finance_app/features/sms/application/sms_controller.dart';
import 'package:finance_app/features/sms/data/model/transaction_draft.dart';
import 'package:finance_app/features/sync/sync_controller.dart';
import 'package:finance_app/features/transaction/data/model/transaction.dart';
import 'package:finance_app/features/transaction/ui/transaction_form_page.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:intl/intl.dart';
import 'package:permission_handler/permission_handler.dart';
import 'package:finance_app/widgets/app_page_header.dart';
class SmsReviewPage extends ConsumerStatefulWidget {
  const SmsReviewPage({super.key});

  @override
  ConsumerState<SmsReviewPage> createState() => _SmsReviewPageState();
}

class _SmsReviewPageState extends ConsumerState<SmsReviewPage>
    with SingleTickerProviderStateMixin {
  late final AnimationController _selectionBarController;
  late final Animation<double> _selectionBarScale;

  @override
  void initState() {
    super.initState();
    _requestSmsPermission();
    _selectionBarController = AnimationController(
      duration: const Duration(milliseconds: 240),
      vsync: this,
    );
    _selectionBarScale = CurvedAnimation(
      parent: _selectionBarController,
      curve: Curves.easeOutBack,
    );
  }

  @override
  void dispose() {
    _selectionBarController.dispose();
    super.dispose();
  }

  Future<void> _requestSmsPermission() async {
    await Permission.sms.request();
  }

  Future<void> _refreshDrafts() async {
    await ref.read(smsControllerProvider.notifier).refresh();
  }

  Future<void> _startSync() async {
    final syncController = ref.read(syncControllerProvider.notifier);

    // Always show the range sheet — returns null (cancelled), 0 (continue), or timestamp (range override)
    final result = await _showSyncRangeSheet(syncController);
    if (result == null) return; // user cancelled

    final bootstrapOverride = result > 0 ? result : null;
    final error = await syncController.startSync(
      bootstrapStartTimestampMillis: bootstrapOverride,
    );
    if (error != null && mounted) {
      _showSnackBar(error, isError: true);
    }
  }

  Future<void> _toggleAutoSync(bool enabled) async {
    final error = await ref
        .read(syncControllerProvider.notifier)
        .setAutoSyncEnabled(enabled);
    if (error != null && mounted) {
      _showSnackBar(error, isError: true);
      return;
    }

    if (!mounted) return;
    _showSnackBar(
      enabled
          ? 'Auto sync enabled. The app will scan every 6 hours.'
          : 'Auto sync disabled. Manual sync is still available.',
    );
  }

  Future<void> _editDraft(TransactionDraft draft) async {
    final transaction = Transaction(
      transactionName: draft.transactionName,
      amount: draft.amount,
      type: draft.type,
      accountId: draft.accountId ?? '',
      categoryId: draft.categoryId ?? '',
      occurredAt: draft.occurredAt,
      currency: draft.currency,
      notes: draft.notes,
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

    if (result is! Transaction) return;

    ref.read(smsControllerProvider.notifier).updateDraft(draft.id, result);
    if (mounted) {
      _showSnackBar('Draft updated.');
    }
  }

  Future<void> _confirmDrafts(List<String> ids) async {
    final draftsState = ref.read(smsControllerProvider);
    if (!draftsState.hasValue) return;

    final draftsToConfirm = draftsState.value!
        .where((draft) => ids.contains(draft.id))
        .toList();

    final invalidDrafts = draftsToConfirm.where((draft) {
      return (draft.accountId == null || draft.accountId!.isEmpty) ||
          (draft.categoryId == null || draft.categoryId!.isEmpty);
    }).toList();

    if (invalidDrafts.isNotEmpty) {
      if (!mounted) return;
      await showDialog<void>(
        context: context,
        builder: (context) => AlertDialog(
          title: Text('Missing details'),
          content: Text(
            'Please choose an account and category for ${invalidDrafts.length} draft(s) before confirming.',
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context),
              child: Text('OK'),
            ),
          ],
        ),
      );
      return;
    }

    await ref.read(smsControllerProvider.notifier).confirmDrafts(ids);
    if (mounted) {
      _showSnackBar('${ids.length} draft(s) confirmed.');
    }
  }

  Future<bool> _confirmDeleteDraft(TransactionDraft draft) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
        title: Text(
          'Delete Draft',
          style: GoogleFonts.plusJakartaSans(
            fontWeight: FontWeight.w700,
            fontSize: 18,
          ),
        ),
        content: Text(
          'Are you sure you want to delete "${draft.transactionName}"? This cannot be undone.',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(
              'Cancel',
              style: GoogleFonts.plusJakartaSans(
                fontWeight: FontWeight.w600,
                color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
              ),
            ),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: TextButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error.withValues(alpha: 0.1),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            child: Text(
              'Delete',
              style: GoogleFonts.plusJakartaSans(
                fontWeight: FontWeight.w600,
                color: Theme.of(context).colorScheme.error,
              ),
            ),
          ),
        ],
      ),
    );

    if (confirmed != true) return false;

    try {
      await ref.read(smsControllerProvider.notifier).deleteDrafts([draft.id]);
      if (mounted) {
        _showSnackBar('"${draft.transactionName}" deleted.');
      }
      return true;
    } catch (e) {
      if (mounted) {
        _showSnackBar('Failed to delete: $e', isError: true);
      }
      return false;
    }
  }

  Future<void> _deleteSelectedDrafts(List<TransactionDraft> selectedDrafts) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (ctx) => AlertDialog(
        shape: RoundedRectangleBorder(
          borderRadius: BorderRadius.circular(20),
        ),
        title: Text(
          'Delete ${selectedDrafts.length} Draft${selectedDrafts.length == 1 ? '' : 's'}',
          style: GoogleFonts.plusJakartaSans(
            fontWeight: FontWeight.w700,
            fontSize: 18,
          ),
        ),
        content: Text(
          'Are you sure you want to delete ${selectedDrafts.length} selected draft${selectedDrafts.length == 1 ? '' : 's'}? This cannot be undone.',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
          ),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(ctx, false),
            child: Text(
              'Cancel',
              style: GoogleFonts.plusJakartaSans(
                fontWeight: FontWeight.w600,
                color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
              ),
            ),
          ),
          TextButton(
            onPressed: () => Navigator.pop(ctx, true),
            style: TextButton.styleFrom(
              backgroundColor: Theme.of(context).colorScheme.error.withValues(alpha: 0.1),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(12),
              ),
            ),
            child: Text(
              'Delete All',
              style: GoogleFonts.plusJakartaSans(
                fontWeight: FontWeight.w600,
                color: Theme.of(context).colorScheme.error,
              ),
            ),
          ),
        ],
      ),
    );

    if (confirmed != true) return;

    final ids = selectedDrafts.map((d) => d.id).toList();
    try {
      await ref.read(smsControllerProvider.notifier).deleteDrafts(ids);
      if (mounted) {
        _showSnackBar('${ids.length} draft(s) deleted.');
      }
    } catch (e) {
      if (mounted) {
        _showSnackBar('Failed to delete drafts: $e', isError: true);
      }
    }
  }

  void _handleSyncStateChange(SyncState? previous, SyncState next) async {
    if (!mounted) return;

    final previousStatus = previous?.status;
    if (previousStatus == next.status) return;

    if (next.status == SyncStatus.success) {
      await _refreshDrafts();
      if (!mounted) return;
      final count = next.syncedCount ?? 0;
      _showSnackBar(
        count == 0
            ? 'Sync finished. No new transaction messages were found.'
            : 'Sync finished. $count message(s) were uploaded for drafting.',
      );
      ref.read(syncControllerProvider.notifier).clearTransientStatus();
    } else if (next.status == SyncStatus.error && next.errorMessage != null) {
      _showSnackBar(next.errorMessage!, isError: true);
    }
  }

  void _showSnackBar(String message, {bool isError = false}) {
    ScaffoldMessenger.of(context).showSnackBar(
      SnackBar(
        content: Text(message),
        backgroundColor: isError ? Theme.of(context).colorScheme.error : null,
      ),
    );
  }

  Future<int?> _showSyncRangeSheet(
    SyncController syncController,
  ) async {
    final isFirstSync = syncController.isFirstSync;

    return showModalBottomSheet<int>(
      context: context,
      backgroundColor: Colors.transparent,
      isScrollControlled: true,
      builder: (context) {
        return Container(
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surface,
            borderRadius: BorderRadius.vertical(top: Radius.circular(32)),
          ),
          padding: EdgeInsets.fromLTRB(24, 20, 24, 32),
          child: SafeArea(
            top: false,
            child: Column(
              mainAxisSize: MainAxisSize.min,
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Center(
                  child: Container(
                    width: 44,
                    height: 5,
                    decoration: BoxDecoration(
                      color: Theme.of(context).dividerColor,
                      borderRadius: BorderRadius.circular(999),
                    ),
                  ),
                ),
                SizedBox(height: 22),
                Text(
                  'Choose sync range',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 22,
                    fontWeight: FontWeight.w800,
                    color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
                    letterSpacing: -0.4,
                  ),
                ),
                SizedBox(height: 8),
                Text(
                  'Pick how far back to scan your SMS inbox. Choosing a range will override the last sync watermark.',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 13,
                    fontWeight: FontWeight.w500,
                    color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                    height: 1.45,
                  ),
                ),
                SizedBox(height: 20),
                if (!isFirstSync) ...[
                  _buildBootstrapOption(
                    title: 'Continue from last sync',
                    subtitle: 'Pick up right where the last scan finished.',
                    tag: 'FASTEST',
                    tagColor: Theme.of(context).colorScheme.secondary,
                    onTap: () {
                      Navigator.pop(context, 0); // sentinel: use backend watermark
                    },
                  ),
                  SizedBox(height: 12),
                ],
                _buildBootstrapOption(
                  title: 'Last 7 days',
                  subtitle: 'Quick pass with minimal draft noise.',
                  tag: 'QUICK',
                  tagColor: Theme.of(context).colorScheme.secondary,
                  onTap: () {
                    Navigator.pop(
                      context,
                      syncController.bootstrapStartTimestampMillisForDays(7),
                    );
                  },
                ),
                SizedBox(height: 12),
                _buildBootstrapOption(
                  title: 'Last 30 days',
                  subtitle: 'Best starting point for most users.',
                  tag: 'RECOMMENDED',
                  tagColor: Theme.of(context).colorScheme.primary,
                  onTap: () {
                    Navigator.pop(
                      context,
                      syncController.bootstrapStartTimestampMillisForDays(30),
                    );
                  },
                ),
                SizedBox(height: 12),
                _buildBootstrapOption(
                  title: 'Last 90 days',
                  subtitle: 'Recover more history with a larger sync.',
                  tag: 'DEEPER',
                  tagColor: Theme.of(context).colorScheme.secondary,
                  onTap: () {
                    Navigator.pop(
                      context,
                      syncController.bootstrapStartTimestampMillisForDays(90),
                    );
                  },
                ),
                SizedBox(height: 16),
                TextButton(
                  onPressed: () => Navigator.pop(context),
                  child: Text(
                    'Cancel',
                    style: GoogleFonts.plusJakartaSans(
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildBootstrapOption({
    required String title,
    required String subtitle,
    required String tag,
    required Color tagColor,
    required VoidCallback onTap,
  }) {
    return InkWell(
      onTap: onTap,
      borderRadius: BorderRadius.circular(24),
      child: Ink(
        padding: EdgeInsets.all(18),
        decoration: BoxDecoration(
          color: Theme.of(context).scaffoldBackgroundColor,
          borderRadius: BorderRadius.circular(24),
          border: Border.all(color: Theme.of(context).dividerColor),
        ),
        child: Row(
          children: [
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 16,
                      fontWeight: FontWeight.w800,
                      color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
                    ),
                  ),
                  SizedBox(height: 6),
                  Text(
                    subtitle,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      fontWeight: FontWeight.w500,
                      color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                      height: 1.35,
                    ),
                  ),
                ],
              ),
            ),
            SizedBox(width: 12),
            Column(
              crossAxisAlignment: CrossAxisAlignment.end,
              children: [
                Container(
                  padding: EdgeInsets.symmetric(
                    horizontal: 10,
                    vertical: 6,
                  ),
                  decoration: BoxDecoration(
                    color: tagColor.withValues(alpha: 0.14),
                    borderRadius: BorderRadius.circular(999),
                  ),
                  child: Text(
                    tag,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 10,
                      fontWeight: FontWeight.w800,
                      letterSpacing: 0.8,
                      color: tagColor,
                    ),
                  ),
                ),
                SizedBox(height: 12),
                Icon(
                  Icons.arrow_forward_rounded,
                  color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                  size: 20,
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    ref.listen<SyncState>(syncControllerProvider, _handleSyncStateChange);

    final draftsState = ref.watch(smsControllerProvider);
    final syncState = ref.watch(syncControllerProvider);
    final checkedDrafts =
        draftsState.asData?.value.where((draft) => draft.isChecked).toList() ??
        [];

    if (checkedDrafts.isNotEmpty) {
      _selectionBarController.forward();
    } else {
      _selectionBarController.reverse();
    }

    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: Stack(
        children: [
          Column(
            children: [
              const AppPageHeader(title: 'SMS Review'),
              Expanded(
                child: draftsState.when(
                  data: (drafts) => RefreshIndicator(
                    onRefresh: _refreshDrafts,
                    child: ListView(
                      padding: EdgeInsets.fromLTRB(24, 8, 24, 140),
                      children: [
                        _buildSyncHero(syncState),
                        SizedBox(height: 16),
                        _buildAutoSyncCard(syncState),
                        SizedBox(height: 16),
                        if (drafts.isEmpty)
                          _buildEmptyState()
                        else ...[
                          _buildDraftsListHeader(drafts.length),
                          SizedBox(height: 12),
                          ...drafts.map(_buildDraftCard),
                        ],
                      ],
                    ),
                  ),
                  loading: () =>
                      Center(child: CircularProgressIndicator()),
                  error: (error, _) => _buildErrorState(error),
                ),
              ),
            ],
          ),
          Positioned(
            left: 24,
            right: 24,
            bottom: 32,
            child: ScaleTransition(
              scale: _selectionBarScale,
              child: _buildFloatingSelectionBar(checkedDrafts),
            ),
          ),
        ],
      ),
    );
  }


  Widget _buildSyncHero(SyncState syncState) {
    final isSyncing = syncState.status == SyncStatus.syncing;
    final isSuccess = syncState.status == SyncStatus.success;
    final isError = syncState.status == SyncStatus.error;

    String badge;
    String subtitle;
    Color badgeColor;

    if (isSyncing) {
      badge = 'RUNNING';
      subtitle = 'Scanning your inbox and uploading likely transaction messages.';
      badgeColor = const Color(0xFFF59E0B);
    } else if (isError) {
      badge = 'NEEDS ATTENTION';
      subtitle =
          syncState.errorMessage ?? 'The last sync did not finish successfully.';
      badgeColor = Theme.of(context).colorScheme.error;
    } else if (isSuccess) {
      badge = 'LAST RUN OK';
      subtitle =
          '${syncState.syncedCount ?? 0} message(s) were handed off for draft creation.';
      badgeColor = Theme.of(context).colorScheme.primary;
    } else if (syncState.lastSyncedAt != null) {
      badge = 'READY';
      subtitle =
          'Last sync ${_formatTimeAgo(syncState.lastSyncedAt!)}. Run a fresh scan any time.';
      badgeColor = Theme.of(context).colorScheme.primary;
    } else {
      badge = 'NOT RUN YET';
      subtitle =
          'Your first sync will scan the inbox, upload candidate messages, and fetch drafts here.';
      badgeColor = (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey);
    }

    return Container(
      decoration: BoxDecoration(
        gradient: LinearGradient(
          begin: Alignment.topLeft,
          end: Alignment.bottomRight,
          colors: [Theme.of(context).colorScheme.primary.withValues(alpha: 0.9), Theme.of(context).colorScheme.primary.withValues(alpha: 0.7)],
        ),
        borderRadius: BorderRadius.circular(32),
        boxShadow: [
          BoxShadow(
            color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black).withValues(alpha: 0.15),
            blurRadius: 24,
            offset: const Offset(0, 14),
          ),
        ],
      ),
      padding: EdgeInsets.all(24),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      isSyncing ? 'Sync in progress' : 'Periodic inbox sync',
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 24,
                        fontWeight: FontWeight.w800,
                        color: Theme.of(context).colorScheme.surface,
                        letterSpacing: -0.5,
                      ),
                    ),
                    SizedBox(height: 8),
                    Text(
                      subtitle,
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 13,
                        fontWeight: FontWeight.w500,
                        color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                        height: 1.45,
                      ),
                    ),
                  ],
                ),
              ),
              SizedBox(width: 16),
              Container(
                width: 58,
                height: 58,
                decoration: BoxDecoration(
                  color: Theme.of(context).colorScheme.surface.withValues(alpha: 0.08),
                  borderRadius: BorderRadius.circular(18),
                  border: Border.all(
                    color: Theme.of(context).colorScheme.surface.withValues(alpha: 0.08),
                  ),
                ),
                child: isSyncing
                    ? Padding(
                        padding: EdgeInsets.all(14),
                        child: CircularProgressIndicator(
                          strokeWidth: 2.6,
                          color: Theme.of(context).colorScheme.surface,
                        ),
                      )
                    : Icon(
                        isError
                            ? Icons.error_outline
                            : isSuccess
                            ? Icons.check_circle_outline
                            : Icons.sync_outlined,
                        color: Theme.of(context).colorScheme.surface,
                        size: 28,
                      ),
              ),
            ],
          ),
          SizedBox(height: 22),
          Wrap(
            spacing: 12,
            runSpacing: 12,
            children: [
              _buildSyncMetric(
                label: 'Source',
                value: 'Inbox scan',
                accent: const Color(0xFFF59E0B),
              ),
              _buildSyncMetric(
                label: 'Draft flow',
                value: 'Review first',
                accent: const Color(0xFF34D399),
              ),
            ],
          ),
          SizedBox(height: 22),
          SizedBox(
            width: double.infinity,
            child: ElevatedButton(
              onPressed: isSyncing ? null : _startSync,
              style: ElevatedButton.styleFrom(
                backgroundColor: Theme.of(context).colorScheme.surface,
                foregroundColor: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
                disabledBackgroundColor: Theme.of(context).colorScheme.surface.withValues(alpha: 0.75),
                elevation: 0,
                padding: EdgeInsets.symmetric(vertical: 16),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(18),
                ),
              ),
              child: Text(
                isSyncing ? 'Syncing now...' : 'Run sync now',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 15,
                  fontWeight: FontWeight.w800,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildSyncMetric({
    required String label,
    required String value,
    required Color accent,
  }) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 14, vertical: 12),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface.withValues(alpha: 0.06),
        borderRadius: BorderRadius.circular(18),
        border: Border.all(color: Theme.of(context).colorScheme.surface.withValues(alpha: 0.06)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            label,
            style: GoogleFonts.plusJakartaSans(
              fontSize: 11,
              fontWeight: FontWeight.w700,
              color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
              letterSpacing: 0.6,
            ),
          ),
          SizedBox(height: 4),
          Text(
            value,
            style: GoogleFonts.plusJakartaSans(
              fontSize: 14,
              fontWeight: FontWeight.w800,
              color: accent,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildAutoSyncCard(SyncState syncState) {
    return Container(
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: Theme.of(context).dividerColor),
        boxShadow: [
          BoxShadow(
            color: Colors.black.withValues(alpha: 0.03),
            blurRadius: 16,
            offset: const Offset(0, 8),
          ),
        ],
      ),
      padding: EdgeInsets.all(20),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                width: 46,
                height: 46,
                decoration: BoxDecoration(
                  color: syncState.autoSyncEnabled
                      ? Theme.of(context).colorScheme.primary.withValues(alpha: 0.1)
                      : Theme.of(context).colorScheme.surface.withValues(alpha: 0.08),
                  borderRadius: BorderRadius.circular(16),
                ),
                child: Icon(
                  syncState.autoSyncEnabled
                      ? Icons.schedule_send_rounded
                      : Icons.pause_circle_outline_rounded,
                  color: syncState.autoSyncEnabled
                      ? Theme.of(context).colorScheme.primary
                      : (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                ),
              ),
              SizedBox(width: 14),
              Expanded(
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Text(
                      'Auto sync every 6 hours',
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 16,
                        fontWeight: FontWeight.w800,
                        color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
                      ),
                    ),
                    SizedBox(height: 4),
                    Text(
                      syncState.autoSyncEnabled
                          ? 'Scheduled scans will keep drafts flowing in even when the app is closed.'
                          : 'Turn this on to keep scanning the inbox in the background.',
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 12,
                        fontWeight: FontWeight.w500,
                        color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                        height: 1.35,
                      ),
                    ),
                  ],
                ),
              ),
              Switch(
                value: syncState.autoSyncEnabled,
                onChanged: _toggleAutoSync,
                activeThumbColor: Theme.of(context).colorScheme.primary,
              ),
            ],
          ),
        ],
      ),
    );
  }

  Widget _buildEmptyState() {
    return Container(
      padding: EdgeInsets.fromLTRB(20, 28, 20, 20),
      decoration: BoxDecoration(
        color: Theme.of(context).colorScheme.surface,
        borderRadius: BorderRadius.circular(28),
        border: Border.all(color: Theme.of(context).dividerColor),
      ),
      child: Column(
        children: [
          Container(
            width: 78,
            height: 78,
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.surface.withValues(alpha: 0.08),
              borderRadius: BorderRadius.circular(24),
            ),
            child: Icon(
              Icons.inbox_rounded,
              size: 34,
              color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
            ),
          ),
          SizedBox(height: 18),
          Text(
            'No draft transactions yet',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 18,
              fontWeight: FontWeight.w800,
              color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
            ),
          ),
          SizedBox(height: 8),
          Text(
            'Run a sync to scan your SMS inbox. Drafts will appear here for review before they affect your ledger.',
            textAlign: TextAlign.center,
            style: GoogleFonts.plusJakartaSans(
              fontSize: 13,
              fontWeight: FontWeight.w500,
              color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
              height: 1.5,
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildErrorState(Object error) {
    return Center(
      child: Padding(
        padding: EdgeInsets.all(24),
        child: Column(
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(Icons.error_outline, color: Theme.of(context).colorScheme.error.withValues(alpha: 0.4), size: 48),
            SizedBox(height: 12),
            Text(
              'Unable to load SMS drafts',
              style: GoogleFonts.plusJakartaSans(
                fontSize: 16,
                fontWeight: FontWeight.w700,
                color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
              ),
            ),
            SizedBox(height: 8),
            Text(
              error.toString(),
              textAlign: TextAlign.center,
              style: GoogleFonts.plusJakartaSans(
                fontSize: 12,
                fontWeight: FontWeight.w500,
                color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
              ),
            ),
            SizedBox(height: 16),
            ElevatedButton(
              onPressed: _refreshDrafts,
              child: Text('Retry'),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildDraftsListHeader(int count) {
    return Padding(
      padding: EdgeInsets.symmetric(horizontal: 4),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Text(
            'PENDING REVIEW',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w900,
              letterSpacing: 1.4,
              color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
            ),
          ),
          Container(
            padding: EdgeInsets.symmetric(horizontal: 12, vertical: 8),
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.1),
              borderRadius: BorderRadius.circular(999),
            ),
            child: Text(
              '$count draft${count == 1 ? '' : 's'}',
              style: GoogleFonts.plusJakartaSans(
                fontSize: 11,
                fontWeight: FontWeight.w800,
                color: Theme.of(context).colorScheme.primary,
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildDraftCard(TransactionDraft draft) {
    final isSelected = draft.isChecked;
    final isIncome = draft.type.toUpperCase() == 'INCOME';
    final color = isIncome
        ? Theme.of(context).colorScheme.primary
        : Theme.of(context).colorScheme.primary;
    final amountColor = isIncome
        ? Theme.of(context).colorScheme.primary
        : Theme.of(context).colorScheme.error;
    final pillColor = isIncome
        ? Theme.of(context).colorScheme.primary.withValues(alpha: 0.1)
        : Theme.of(context).colorScheme.primary.withValues(alpha: 0.1);

    final accountName = _resolveAccountName(draft.accountId);
    final categoryName = _resolveCategoryName(draft.categoryId);

    return Dismissible(
      key: Key(draft.id),
      direction: DismissDirection.endToStart,
      confirmDismiss: (_) => _confirmDeleteDraft(draft),
      background: Container(
        margin: EdgeInsets.only(bottom: 16),
        padding: EdgeInsets.only(right: 28),
        alignment: Alignment.centerRight,
        decoration: BoxDecoration(
          color: Theme.of(context).colorScheme.error,
          borderRadius: BorderRadius.circular(28),
        ),
        child: Icon(Icons.delete_outline, color: Theme.of(context).colorScheme.surface, size: 28),
      ),
      child: GestureDetector(
        onTap: () {
          ref
              .read(smsControllerProvider.notifier)
              .toggleDraft(draft.id, !isSelected);
        },
        child: Container(
          margin: EdgeInsets.only(bottom: 16),
          padding: EdgeInsets.all(20),
          decoration: BoxDecoration(
            color: Theme.of(context).colorScheme.surface,
            borderRadius: BorderRadius.circular(28),
            border: Border.all(
              color: isSelected ? color : Theme.of(context).dividerColor,
              width: isSelected ? 2 : 1,
            ),
            boxShadow: [
              BoxShadow(
                color: color.withValues(alpha: isSelected ? 0.14 : 0.03),
                blurRadius: isSelected ? 24 : 12,
                offset: const Offset(0, 8),
              ),
            ],
          ),
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Row(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Stack(
                    clipBehavior: Clip.none,
                    children: [
                      Container(
                        width: 56,
                        height: 56,
                        decoration: BoxDecoration(
                          color: pillColor,
                          borderRadius: BorderRadius.circular(18),
                        ),
                        child: Icon(
                          draft.categoryIcon ??
                              (isIncome
                                  ? Icons.account_balance_wallet_outlined
                                  : Icons.receipt_long_outlined),
                          color: color,
                          size: 28,
                        ),
                      ),
                      if (isSelected)
                        Positioned(
                          top: -4,
                          right: -4,
                          child: Container(
                            width: 20,
                            height: 20,
                            decoration: BoxDecoration(
                              color: color,
                              shape: BoxShape.circle,
                              border: Border.all(
                                color: Theme.of(context).colorScheme.surface,
                                width: 2,
                              ),
                            ),
                            child: Icon(
                              Icons.check,
                              size: 12,
                              color: Theme.of(context).colorScheme.surface,
                            ),
                          ),
                        ),
                    ],
                  ),
                  SizedBox(width: 14),
                  Expanded(
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.start,
                      children: [
                        Text(
                          draft.transactionName,
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 16,
                            fontWeight: FontWeight.w800,
                            color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
                            height: 1.2,
                          ),
                        ),
                        SizedBox(height: 6),
                        Text(
                          DateFormat('MMM d, h:mm a').format(draft.occurredAt),
                          style: GoogleFonts.plusJakartaSans(
                            fontSize: 12,
                            fontWeight: FontWeight.w600,
                            color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                          ),
                        ),
                      ],
                    ),
                  ),
                  SizedBox(width: 12),
                  Text(
                    '${isIncome ? '+' : '-'}Rs ${NumberFormat('#,##,##0.00').format(draft.amount)}',
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 18,
                      fontWeight: FontWeight.w900,
                      color: amountColor,
                    ),
                  ),
                ],
              ),
              SizedBox(height: 18),
              Row(
                children: [
                  Expanded(
                    child: _buildInfoPill(
                      icon: Icons.grid_view_rounded,
                      label: categoryName,
                    ),
                  ),
                  SizedBox(width: 10),
                  Expanded(
                    child: _buildInfoPill(
                      icon: Icons.account_balance_wallet_outlined,
                      label: accountName,
                    ),
                  ),
                ],
              ),
              SizedBox(height: 14),
              Container(
                width: double.infinity,
                padding: EdgeInsets.all(14),
                decoration: BoxDecoration(
                  color: pillColor,
                  borderRadius: BorderRadius.circular(18),
                ),
                child: Text(
                  draft.originalMessage,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w600,
                    fontStyle: FontStyle.italic,
                    color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                    height: 1.45,
                  ),
                ),
              ),
              SizedBox(height: 16),
              Row(
                children: [
                  Expanded(
                    child: OutlinedButton.icon(
                      onPressed: () => _editDraft(draft),
                      style: OutlinedButton.styleFrom(
                        padding: EdgeInsets.symmetric(vertical: 16),
                        side: BorderSide(color: Theme.of(context).dividerColor),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                        foregroundColor: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                      ),
                      icon: Icon(Icons.edit_outlined, size: 18),
                      label: Text('Edit'),
                    ),
                  ),
                  SizedBox(width: 12),
                  Expanded(
                    flex: 2,
                    child: ElevatedButton.icon(
                      onPressed: () => _confirmDrafts([draft.id]),
                      style: ElevatedButton.styleFrom(
                        backgroundColor: color,
                        foregroundColor: Theme.of(context).colorScheme.surface,
                        padding: EdgeInsets.symmetric(vertical: 16),
                        shape: RoundedRectangleBorder(
                          borderRadius: BorderRadius.circular(16),
                        ),
                        elevation: 0,
                      ),
                      icon: Icon(Icons.check, size: 18),
                      label: Text(
                        isIncome ? 'Confirm income' : 'Confirm expense',
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

  Widget _buildInfoPill({required IconData icon, required String label}) {
    return Container(
      padding: EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: Theme.of(context).scaffoldBackgroundColor,
        borderRadius: BorderRadius.circular(16),
        border: Border.all(color: Theme.of(context).dividerColor),
      ),
      child: Row(
        children: [
          Icon(icon, size: 14, color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey)),
          SizedBox(width: 8),
          Expanded(
            child: Text(
              label,
              overflow: TextOverflow.ellipsis,
              style: GoogleFonts.plusJakartaSans(
                fontSize: 12,
                fontWeight: FontWeight.w700,
                color: (Theme.of(context).textTheme.titleLarge?.color ?? Colors.black),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildFloatingSelectionBar(List<TransactionDraft> checkedDrafts) {
    if (checkedDrafts.isEmpty) {
      return SizedBox.shrink();
    }

    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        gradient: LinearGradient(
          colors: [Theme.of(context).colorScheme.primary, Theme.of(context).colorScheme.primary],
        ),
        borderRadius: BorderRadius.circular(24),
        boxShadow: [
          BoxShadow(
            color: Theme.of(context).colorScheme.primary.withValues(alpha: 0.35),
            blurRadius: 24,
            offset: const Offset(0, 12),
          ),
        ],
      ),
      child: Row(
        children: [
          Container(
            width: 42,
            height: 42,
            decoration: BoxDecoration(
              color: Theme.of(context).colorScheme.surface.withValues(alpha: 0.14),
              borderRadius: BorderRadius.circular(14),
            ),
            child: Icon(
              Icons.playlist_add_check,
              color: Theme.of(context).colorScheme.surface,
              size: 20,
            ),
          ),
          SizedBox(width: 12),
          Expanded(
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Text(
                  '${checkedDrafts.length} draft${checkedDrafts.length == 1 ? '' : 's'} selected',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w800,
                    color: Theme.of(context).colorScheme.surface,
                  ),
                ),
                Text(
                  'Confirm to ledger or delete permanently.',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 11,
                    fontWeight: FontWeight.w500,
                    color: (Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey),
                  ),
                ),
              ],
            ),
          ),
          SizedBox(width: 8),
          InkWell(
            onTap: () => _deleteSelectedDrafts(checkedDrafts),
            borderRadius: BorderRadius.circular(14),
            child: Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.error.withValues(alpha: 0.25),
                borderRadius: BorderRadius.circular(14),
              ),
              child: Icon(
                Icons.delete_outline_rounded,
                color: Theme.of(context).colorScheme.surface,
                size: 20,
              ),
            ),
          ),
          SizedBox(width: 8),
          InkWell(
            onTap: () {
              final ids = checkedDrafts.map((draft) => draft.id).toList();
              _confirmDrafts(ids);
            },
            borderRadius: BorderRadius.circular(16),
            child: Container(
              padding: EdgeInsets.symmetric(horizontal: 18, vertical: 12),
              decoration: BoxDecoration(
                color: Theme.of(context).colorScheme.surface,
                borderRadius: BorderRadius.circular(16),
              ),
              child: Text(
                'Confirm',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 12,
                  fontWeight: FontWeight.w800,
                  color: Theme.of(context).colorScheme.primary,
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  String _resolveAccountName(String? accountId) {
    if (accountId == null || accountId.isEmpty) {
      return 'Choose account';
    }

    final accounts = ref.read(accountsControllerProvider).asData?.value ?? [];
    for (final account in accounts) {
      if (account.id == accountId) {
        return account.accountName;
      }
    }
    return accountId;
  }

  String _resolveCategoryName(String? categoryId) {
    if (categoryId == null || categoryId.isEmpty) {
      return 'Choose category';
    }

    final categories = ref.read(childrenCategoriesProvider).asData?.value ?? [];
    for (final category in categories) {
      if (category.id == categoryId) {
        return category.name;
      }
    }
    return categoryId;
  }

  String _formatTimeAgo(DateTime dateTime) {
    final diff = DateTime.now().difference(dateTime);
    if (diff.inMinutes < 1) return 'just now';
    if (diff.inHours < 1) return '${diff.inMinutes}m ago';
    if (diff.inDays < 1) return '${diff.inHours}h ago';
    if (diff.inDays < 7) return '${diff.inDays}d ago';
    return DateFormat('MMM d').format(dateTime);
  }
}
