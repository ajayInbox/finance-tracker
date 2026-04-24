import 'dart:async';

import 'package:finance_app/features/sync/sync_permission_helper.dart';
import 'package:finance_app/platform/sync_service.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:shared_preferences/shared_preferences.dart';

enum SyncStatus { idle, syncing, success, error }

class SyncState {
  final SyncStatus status;
  final String? errorMessage;
  final int? syncedCount;
  final DateTime? lastSyncedAt;
  final bool autoSyncEnabled;

  const SyncState({
    this.status = SyncStatus.idle,
    this.errorMessage,
    this.syncedCount,
    this.lastSyncedAt,
    this.autoSyncEnabled = false,
  });

  SyncState copyWith({
    SyncStatus? status,
    String? errorMessage,
    int? syncedCount,
    DateTime? lastSyncedAt,
    bool? autoSyncEnabled,
  }) {
    return SyncState(
      status: status ?? this.status,
      errorMessage: errorMessage ?? this.errorMessage,
      syncedCount: syncedCount ?? this.syncedCount,
      lastSyncedAt: lastSyncedAt ?? this.lastSyncedAt,
      autoSyncEnabled: autoSyncEnabled ?? this.autoSyncEnabled,
    );
  }
}

final syncControllerProvider =
    NotifierProvider<SyncController, SyncState>(SyncController.new);

class SyncController extends Notifier<SyncState> {
  static const _lastSyncKey = 'last_sync_timestamp';
  static const autoSyncFrequencyHours = 6;

  StreamSubscription<String>? _statusSub;

  @override
  SyncState build() {
    _loadPersistedState();

    _statusSub = SyncService.statusStream().listen(_handleStatusEvent);
    ref.onDispose(() => _statusSub?.cancel());

    return const SyncState();
  }

  bool get isFirstSync => state.lastSyncedAt == null;

  int bootstrapStartTimestampMillisForDays(int days) {
    return DateTime.now()
        .subtract(Duration(days: days))
        .millisecondsSinceEpoch;
  }

  Future<String?> startSync({
    int? bootstrapStartTimestampMillis,
  }) async {
    if (state.status == SyncStatus.syncing) return 'Sync already in progress';

    final result = await SyncPermissionHelper.requestAll();
    if (!result.allGranted) {
      final missing = result.missingPermissions.join(', ');
      return 'Missing permissions: $missing';
    }

    state = state.copyWith(
      status: SyncStatus.syncing,
      errorMessage: null,
    );

    try {
      await SyncService.startManualSync(
        bootstrapStartTimestampMillis: bootstrapStartTimestampMillis,
      );
      return null;
    } catch (e) {
      state = state.copyWith(
        status: SyncStatus.error,
        errorMessage: e.toString(),
      );
      return e.toString();
    }
  }

  Future<String?> setAutoSyncEnabled(bool enabled) async {
    final result = await SyncPermissionHelper.requestAll();
    if (!result.allGranted) {
      final missing = result.missingPermissions.join(', ');
      return 'Missing permissions: $missing';
    }

    try {
      if (enabled) {
        await SyncService.enableAutoSync();
      } else {
        await SyncService.disableAutoSync();
      }

      state = state.copyWith(
        autoSyncEnabled: enabled,
        errorMessage: null,
      );
      return null;
    } catch (e) {
      state = state.copyWith(
        status: SyncStatus.error,
        errorMessage: e.toString(),
      );
      return e.toString();
    }
  }

  void clearTransientStatus() {
    if (state.status == SyncStatus.syncing) return;
    state = state.copyWith(
      status: SyncStatus.idle,
      errorMessage: null,
    );
  }

  void _handleStatusEvent(String raw) {
    if (raw == 'SYNCING') {
      state = state.copyWith(
        status: SyncStatus.syncing,
        errorMessage: null,
      );
      return;
    }

    if (raw.startsWith('SUCCESS:')) {
      final parts = raw.split(':');
      final count = int.tryParse(parts.length > 1 ? parts[1] : '0') ?? 0;
      final ts = int.tryParse(parts.length > 2 ? parts[2] : '0') ?? 0;
      final when = ts > 0
          ? DateTime.fromMillisecondsSinceEpoch(ts)
          : DateTime.now();

      state = state.copyWith(
        status: SyncStatus.success,
        syncedCount: count,
        lastSyncedAt: when,
        errorMessage: null,
      );
      _persistLastSynced(when);
      return;
    }

    if (raw.startsWith('ERROR:')) {
      state = state.copyWith(
        status: SyncStatus.error,
        errorMessage: raw.substring(6),
      );
    }
  }

  Future<void> _loadPersistedState() async {
    final prefs = await SharedPreferences.getInstance();
    final ts = prefs.getInt(_lastSyncKey);
    final autoSyncEnabled = await SyncService.isAutoSyncEnabled();

    var nextState = SyncState(autoSyncEnabled: autoSyncEnabled);
    if (ts != null && ts > 0) {
      nextState = nextState.copyWith(
        lastSyncedAt: DateTime.fromMillisecondsSinceEpoch(ts),
      );
    }
    state = nextState;
  }

  Future<void> _persistLastSynced(DateTime when) async {
    final prefs = await SharedPreferences.getInstance();
    await prefs.setInt(_lastSyncKey, when.millisecondsSinceEpoch);
  }
}
