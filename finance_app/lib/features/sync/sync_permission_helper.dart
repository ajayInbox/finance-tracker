import 'package:permission_handler/permission_handler.dart';

/// Result of checking all permissions needed for SMS sync.
class SyncPermissionResult {
  final bool smsGranted;
  final bool notificationGranted;

  const SyncPermissionResult({
    required this.smsGranted,
    required this.notificationGranted,
  });

  bool get allGranted => smsGranted && notificationGranted;

  List<String> get missingPermissions {
    final missing = <String>[];
    if (!smsGranted) missing.add('SMS');
    if (!notificationGranted) missing.add('Notifications');
    return missing;
  }
}

/// Utility to check and request permissions required for the sync workflow:
///   • READ_SMS / RECEIVE_SMS  (grouped under [Permission.sms])
///   • POST_NOTIFICATIONS      (Android 13+, [Permission.notification])
class SyncPermissionHelper {

  /// Returns current permission status without requesting.
  static Future<SyncPermissionResult> check() async {
    final sms = await Permission.sms.isGranted;
    final notification = await Permission.notification.isGranted;
    return SyncPermissionResult(
      smsGranted: sms,
      notificationGranted: notification,
    );
  }

  /// Requests any missing permissions and returns the final state.
  static Future<SyncPermissionResult> requestAll() async {
    final statuses = await [
      Permission.sms,
      Permission.notification,
    ].request();

    return SyncPermissionResult(
      smsGranted: statuses[Permission.sms]?.isGranted ?? false,
      notificationGranted:
          statuses[Permission.notification]?.isGranted ?? false,
    );
  }
}
