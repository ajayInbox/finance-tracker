import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';

/// Shared header widget used by all top-level tab pages.
///
/// Supply either a plain [title] string (Transactions, Accounts, Settings)
/// or a custom [leading] widget (Dashboard's avatar + greeting row).
/// Trailing actions default to a single [NotificationBell] if omitted.
class AppPageHeader extends StatelessWidget {
  /// Simple text title displayed on the left.
  /// Ignored when [leading] is provided.
  final String? title;

  /// Custom leading widget that replaces the title.
  /// Used by Dashboard for the avatar + greeting row.
  final Widget? leading;

  /// Trailing action widgets. Defaults to `[NotificationBell()]`.
  final List<Widget>? actions;

  /// Padding override. Defaults to consistent app-wide header padding.
  final EdgeInsets padding;

  const AppPageHeader({
    super.key,
    this.title,
    this.leading,
    this.actions,
    this.padding = const EdgeInsets.only(
      left: 24,
      right: 24,
      top: 48,
      bottom: 24,
    ),
  }) : assert(
         title != null || leading != null,
         'Either title or leading must be provided',
       );

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: padding,
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          // ── Leading / Title ──
          if (leading != null)
            Expanded(child: leading!)
          else
            Text(
              title!,
              style: GoogleFonts.plusJakartaSans(
                fontSize: 24,
                fontWeight: FontWeight.w700,
                color: const Color(0xFF111827),
              ),
            ),

          // ── Trailing Actions ──
          Row(
            mainAxisSize: MainAxisSize.min,
            children: actions ?? [const NotificationBell()],
          ),
        ],
      ),
    );
  }
}

/// The notification bell icon with a red badge dot.
///
/// Currently inert — provides an [onTap] hook for future wiring.
class NotificationBell extends StatelessWidget {
  final VoidCallback? onTap;

  const NotificationBell({super.key, this.onTap});

  @override
  Widget build(BuildContext context) {
    return GestureDetector(
      onTap: onTap,
      child: Container(
        width: 48,
        height: 48,
        decoration: BoxDecoration(
          color: Colors.white,
          borderRadius: BorderRadius.circular(16),
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
                color: Color(0xFF4B5563),
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
                  color: Color(0xFFEF4444),
                  shape: BoxShape.circle,
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}
