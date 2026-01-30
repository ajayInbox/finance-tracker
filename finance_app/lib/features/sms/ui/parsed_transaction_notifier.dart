import 'dart:async';
import 'dart:convert';
import 'dart:math';

import 'package:finance_app/features/sms/ui/sms_ui_state.dart';
import 'package:finance_app/platform/parsed_transaction_channel.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

final parsedTxnProvider = AsyncNotifierProvider<ParsedTxnNotifier, SmsUiState>(
  ParsedTxnNotifier.new,
);

class ParsedTxnNotifier extends AsyncNotifier<SmsUiState> {
  StreamSubscription<String>? _sub;

  @override
  Future<SmsUiState> build() async {
    _sub = ParsedTxnChannel.stream().listen((txnJson) {
      _processIncomingJson(txnJson);
    });

    ref.onDispose(() => _sub?.cancel());
    return const SmsUiState();
  }

  void _processIncomingJson(String jsonString) {
    print('----------------------------------------------------------------');
    print('Received JSON: $jsonString');
    print('----------------------------------------------------------------');
    try {
      final Map<String, dynamic> data = jsonDecode(jsonString);

      // Attempt to parse fields.
      final merchant = data['merchant'] ?? data['sender'] ?? 'Unknown Merchant';
      final amountVal = data['amount'];
      final double amount = amountVal is num ? amountVal.toDouble() : 0.0;
      final String body = data['body'] ?? data['message'] ?? '';
      final DateTime date =
          DateTime.tryParse(data['date'] ?? '') ?? DateTime.now();

      final newDraft = TransactionDraft(
        id: _generateId(),
        merchant: merchant,
        date: date,
        amount: amount,
        originalMessage: body,
      );

      _addDraft(newDraft);
    } catch (e) {
      print('Error parsing SMS JSON: $e');
    }
  }

  String _generateId() {
    return '${DateTime.now().millisecondsSinceEpoch}_${Random().nextInt(10000)}';
  }

  void _addDraft(TransactionDraft draft) {
    final currentDrafts = state.value?.drafts ?? [];
    state = AsyncData(
      state.value?.copyWith(drafts: [draft, ...currentDrafts]) ??
          SmsUiState(drafts: [draft]),
    );
  }

  void toggleDraft(String id, bool? isChecked) {
    final currentDrafts = state.value?.drafts ?? [];
    final updatedDrafts = currentDrafts.map((draft) {
      if (draft.id == id) {
        return draft.copyWith(isChecked: isChecked ?? !draft.isChecked);
      }
      return draft;
    }).toList();

    state = AsyncData(
      state.value?.copyWith(drafts: updatedDrafts) ??
          SmsUiState(drafts: updatedDrafts),
    );
  }

  void selectAll() {
    final currentDrafts = state.value?.drafts ?? [];
    // If all are selected, deselect all. Otherwise select all.
    final allSelected = currentDrafts.every((d) => d.isChecked);

    final updatedDrafts = currentDrafts.map((draft) {
      return draft.copyWith(isChecked: !allSelected);
    }).toList();

    state = AsyncData(
      state.value?.copyWith(drafts: updatedDrafts) ??
          SmsUiState(drafts: updatedDrafts),
    );
  }

  void removeDraft(String id) {
    final currentDrafts = state.value?.drafts ?? [];
    final updatedDrafts = currentDrafts.where((d) => d.id != id).toList();
    state = AsyncData(
      state.value?.copyWith(drafts: updatedDrafts) ??
          SmsUiState(drafts: updatedDrafts),
    );
  }

  void clear() {
    state = const AsyncData(SmsUiState());
  }
}
