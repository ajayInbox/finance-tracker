import 'package:flutter/foundation.dart';
import 'package:flutter/services.dart';

class SyncService {
  static const _methodChannel =
      MethodChannel('com.tracker.finance_app/sync');
  static const _eventChannel = EventChannel('sync_status_events');

  static bool get _isSupported =>
      !kIsWeb && defaultTargetPlatform == TargetPlatform.android;

  static Future<void> startManualSync({
    int? bootstrapStartTimestampMillis,
  }) async {
    if (!_isSupported) return;
    await _methodChannel.invokeMethod('startManualSync', {
      'bootstrapStartTimestampMillis': bootstrapStartTimestampMillis,
    });
  }

  static Future<void> enableAutoSync() async {
    if (!_isSupported) return;
    await _methodChannel.invokeMethod('enableAutoSync');
  }

  static Future<void> disableAutoSync() async {
    if (!_isSupported) return;
    await _methodChannel.invokeMethod('disableAutoSync');
  }

  static Future<bool> isAutoSyncEnabled() async {
    if (!_isSupported) return false;
    final enabled = await _methodChannel.invokeMethod<bool>('isAutoSyncEnabled');
    return enabled ?? false;
  }

  static Stream<String> statusStream() {
    if (!_isSupported) return const Stream.empty();
    return _eventChannel.receiveBroadcastStream().cast<String>();
  }
}
