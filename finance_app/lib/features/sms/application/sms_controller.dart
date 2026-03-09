import 'dart:async';

import 'package:finance_app/features/sms/data/model/transaction_draft.dart';
import 'package:finance_app/features/sms/data/repository/sms_repository.dart';
import 'package:finance_app/features/transaction/data/model/transaction.dart';
import 'package:finance_app/features/transaction/data/providers/transaction_repository_provider.dart';
import 'package:finance_app/platform/parsed_transaction_channel.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final smsControllerProvider =
    AsyncNotifierProvider<SmsController, List<TransactionDraft>>(
      SmsController.new,
    );

class SmsController extends AsyncNotifier<List<TransactionDraft>> {
  StreamSubscription<String>? _sub;

  @override
  Future<List<TransactionDraft>> build() async {
    // Listen for incoming SMS events from Android
    _sub = ParsedTxnChannel.stream().listen((_) {
      // On event, refresh from backend
      ref.invalidateSelf();
    });

    ref.onDispose(() => _sub?.cancel());

    final repo = ref.read(smsRepositoryProvider);
    return repo.fetchDrafts();
  }

  void toggleDraft(String id, bool? isChecked) {
    if (!state.hasValue) return;

    final currentDrafts = state.value!;
    final updatedDrafts = currentDrafts.map((draft) {
      if (draft.id == id) {
        return draft.copyWith(
          isChecked: isChecked ?? !draft.isChecked,
          categoryId: draft.categoryId ?? '',
          accountId: draft.accountId ?? '',
        );
      }
      return draft;
    }).toList();

    state = AsyncData(updatedDrafts);
  }

  void selectAll() {
    if (!state.hasValue) return;

    final currentDrafts = state.value!;
    final allSelected = currentDrafts.every((d) => d.isChecked);

    final updatedDrafts = currentDrafts.map((draft) {
      return draft.copyWith(
        isChecked: !allSelected,
        categoryId: draft.categoryId ?? '',
        accountId: draft.accountId ?? '',
      );
    }).toList();

    state = AsyncData(updatedDrafts);
  }

  Future<void> confirmDrafts(List<String> ids) async {
    if (!state.hasValue) return;

    final currentDrafts = state.value!;
    final draftsToConfirm = currentDrafts
        .where((d) => ids.contains(d.id))
        .toList();
    final repo = ref.read(transactionRepositoryProvider);

    // Prepare batch update payload
    final List<Map<String, dynamic>> updates = draftsToConfirm
        .map((draft) {
          if (draft.accountId == null || draft.categoryId == null) {
            // Should be guarded by UI validation, but safety check here
            return <String, dynamic>{};
          }

          return {
            "id": draft.id,
            "transactionName": draft.transactionName,
            "amount": draft.amount,
            "type": draft.type,
            "accountId": draft.accountId,
            "categoryId": draft.categoryId,
            "occurredAt": draft.occurredAt.toIso8601String(),
            "notes": draft.notes.isEmpty ? draft.originalMessage : draft.notes,
            "currency": draft.currency,
          };
        })
        .where((map) => map.isNotEmpty)
        .toList();

    if (updates.isEmpty) return;

    try {
      await repo.updateBatchTransactions(updates);

      // On success, remove confirmed drafts from local state
      // We can remove all that were in our 'updates' list
      final confirmedIds = updates.map((u) => u['id'] as String).toList();

      final updatedDrafts = currentDrafts
          .where((d) => !confirmedIds.contains(d.id))
          .toList();

      state = AsyncData(updatedDrafts);
    } catch (e) {
      // Handle error
      // debugPrint('Error confirming batch: $e');
      rethrow; // Let UI handle/show error if needed
    }
  }

  void removeDraft(String id) {
    if (!state.hasValue) return;

    final currentDrafts = state.value!;
    final updatedDrafts = currentDrafts.where((d) => d.id != id).toList();

    state = AsyncData(updatedDrafts);
  }

  void updateDraft(String id, Transaction tx) {
    if (!state.hasValue) return;

    final currentDrafts = state.value!;
    final updatedDrafts = currentDrafts.map((d) {
      if (d.id == id) {
        return d.copyWith(
          transactionName: tx.transactionName,
          amount: tx.amount,
          type: tx.type,
          accountId: tx.accountId,
          categoryId: tx.categoryId,
          occurredAt: tx.occurredAt,
          notes: tx.notes,
        );
      }
      return d;
    }).toList();

    state = AsyncData(updatedDrafts);
  }
}
