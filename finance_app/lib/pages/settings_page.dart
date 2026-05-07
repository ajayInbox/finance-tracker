import 'package:finance_app/features/category/ui/category_management_page.dart';
import 'package:finance_app/features/auth/application/auth_controller.dart';
import 'package:finance_app/features/auth/data/model/user_profile.dart';
import 'package:finance_app/features/auth/data/providers/user_profile_provider.dart';
import 'package:finance_app/core/theme_provider.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:finance_app/widgets/app_page_header.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:shimmer/shimmer.dart';

class SettingsPage extends ConsumerStatefulWidget {
  const SettingsPage({super.key});

  @override
  ConsumerState<SettingsPage> createState() => _SettingsPageState();
}

class _SettingsPageState extends ConsumerState<SettingsPage> {
  // Toggle states
  bool _faceId = false;
  bool _isLoggingOut = false;

  // App version
  final String _appVersion = '2.4.1 (Build 202)';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: SingleChildScrollView(
        padding: EdgeInsets.only(bottom: 100),
        child: Column(
          children: [
            const AppPageHeader(title: 'Settings'),
            Padding(
              padding: EdgeInsets.symmetric(horizontal: 16), // px-4
              child: Column(
                children: [
                  _buildProfileSection(context),
                  SizedBox(height: 24),
                  _buildFinancesSection(context),
                  SizedBox(height: 24),
                  _buildPreferencesSection(context),
                  SizedBox(height: 24),
                  _buildSecuritySection(context),
                  SizedBox(height: 8),
                  _buildLogoutButton(context),
                  SizedBox(height: 8),
                  Text(
                    'App Version $_appVersion',
                    textAlign: TextAlign.center,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      color: Theme.of(context).textTheme.bodySmall?.color ?? Colors.grey,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }

  Widget _buildProfileSection(BuildContext context) {
    final profileAsync = ref.watch(userProfileProvider);

    return profileAsync.when(
      data: (user) => _buildProfileContent(context, user),
      loading: () => _buildProfileShimmer(context),
      error: (e, _) => _buildProfileError(context),
    );
  }

  Widget _buildProfileContent(BuildContext context, UserProfile user) {
    final theme = Theme.of(context);
    final colorScheme = theme.colorScheme;

    return Column(
      children: [
        // ── Initials Avatar ──
        Container(
          width: 96,
          height: 96,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: colorScheme.surface, width: 4),
            boxShadow: [
              BoxShadow(color: Colors.black.withValues(alpha: 0.1), blurRadius: 10),
            ],
            gradient: LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [colorScheme.primary, colorScheme.primary.withValues(alpha: 0.8)],
            ),
          ),
          child: Center(
            child: Text(
              user.initials,
              style: GoogleFonts.plusJakartaSans(
                fontSize: 32,
                fontWeight: FontWeight.w700,
                color: Colors.white,
                letterSpacing: 1,
              ),
            ),
          ),
        ),
        SizedBox(height: 16),
        Text(
          user.name,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: theme.textTheme.titleLarge?.color,
          ),
        ),
        SizedBox(height: 2),
        Text(
          user.email,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            color: theme.textTheme.bodySmall?.color,
          ),
        ),
        if (user.isSubscribed) ...[
          SizedBox(height: 16),
          Container(
            padding: EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: colorScheme.primary.withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(999),
              border: Border.all(
                color: colorScheme.primary.withValues(alpha: 0.3),
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                Icon(
                  Icons.workspace_premium,
                  size: 14,
                  color: colorScheme.primary,
                ),
                SizedBox(width: 4),
                Text(
                  'Pro Member',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                    color: colorScheme.primary,
                  ),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildProfileShimmer(BuildContext context) {
    final theme = Theme.of(context);
    final isDark = theme.brightness == Brightness.dark;

    return Shimmer.fromColors(
      baseColor: isDark ? Colors.grey[800]! : Colors.grey[300]!,
      highlightColor: isDark ? Colors.grey[700]! : Colors.grey[100]!,
      child: Column(
        children: [
          // Avatar placeholder
          Container(
            width: 96,
            height: 96,
            decoration: BoxDecoration(
              shape: BoxShape.circle,
              color: theme.colorScheme.surface,
            ),
          ),
          SizedBox(height: 16),
          // Name placeholder
          Container(
            width: 140,
            height: 20,
            decoration: BoxDecoration(
              color: theme.colorScheme.surface,
              borderRadius: BorderRadius.circular(4),
            ),
          ),
          SizedBox(height: 8),
          // Email placeholder
          Container(
            width: 200,
            height: 14,
            decoration: BoxDecoration(
              color: theme.colorScheme.surface,
              borderRadius: BorderRadius.circular(4),
            ),
          ),
          SizedBox(height: 16),
          // Badge placeholder
          Container(
            width: 100,
            height: 24,
            decoration: BoxDecoration(
              color: theme.colorScheme.surface,
              borderRadius: BorderRadius.circular(999),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileError(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      children: [
        // Fallback avatar with question mark
        Container(
          width: 96,
          height: 96,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: theme.colorScheme.surface, width: 4),
            boxShadow: [
              BoxShadow(color: Colors.black.withValues(alpha: 0.1), blurRadius: 10),
            ],
            color: theme.dividerColor.withValues(alpha: 0.1),
          ),
          child: Center(
            child: Icon(
              Icons.person_outline_rounded,
              size: 40,
              color: theme.textTheme.bodySmall?.color,
            ),
          ),
        ),
        SizedBox(height: 16),
        Text(
          'Couldn\'t load profile',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            color: theme.textTheme.bodySmall?.color,
          ),
        ),
        SizedBox(height: 8),
        GestureDetector(
          onTap: () => ref.invalidate(userProfileProvider),
          child: Text(
            'Tap to retry',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: theme.colorScheme.primary,
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildFinancesSection(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: EdgeInsets.only(left: 8, bottom: 8),
          child: Text(
            'FINANCES',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: theme.textTheme.bodySmall?.color,
              letterSpacing: 1.0,
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: BorderRadius.circular(16), // rounded-2xl
            border: Border.all(color: theme.dividerColor),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.05),
                blurRadius: 2,
              ),
            ],
          ),
          child: Column(
            children: [
              _buildMenuItem(
                context: context,
                icon: Icons.data_usage,
                iconColor: Colors.orange,
                iconBgColor: Colors.orange.withValues(alpha: 0.1),
                title: 'Spending Limits',
                showBorder: true,
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: Text('Coming soon!'),
                      behavior: SnackBarBehavior.floating,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      backgroundColor: theme.colorScheme.primary,
                      duration: const Duration(seconds: 2),
                    ),
                  );
                },
              ),
              _buildMenuItem(
                context: context,
                icon: Icons.category,
                iconColor: Colors.purple,
                iconBgColor: Colors.purple.withValues(alpha: 0.1),
                title: 'Manage Categories',
                showBorder: false,
                onTap: () {
                  Navigator.of(context).push(
                    MaterialPageRoute(
                      builder: (context) => const CategoryManagementPage(),
                    ),
                  );
                },
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildPreferencesSection(BuildContext context) {
    final theme = Theme.of(context);
    final themeMode = ref.watch(themeProvider);

    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: EdgeInsets.only(left: 8, bottom: 8),
          child: Text(
            'APP PREFERENCES',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: theme.textTheme.bodySmall?.color,
              letterSpacing: 1.0,
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: theme.dividerColor),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.05),
                blurRadius: 2,
              ),
            ],
          ),
          child: Column(
            children: [
              _buildThemeModeItem(
                context: context,
                value: themeMode,
                onChanged: (mode) {
                  ref.read(themeProvider.notifier).setTheme(mode);
                },
              ),
              _buildValueItem(
                context: context,
                icon: Icons.attach_money,
                iconColor: Colors.green,
                iconBgColor: Colors.green.withValues(alpha: 0.1),
                title: 'Currency',
                value: 'USD',
                showBorder: false,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildSecuritySection(BuildContext context) {
    final theme = Theme.of(context);
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: EdgeInsets.only(left: 8, bottom: 8),
          child: Text(
            'SECURITY',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: theme.textTheme.bodySmall?.color,
              letterSpacing: 1.0,
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: theme.colorScheme.surface,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: theme.dividerColor),
            boxShadow: [
              BoxShadow(
                color: Colors.black.withValues(alpha: 0.05),
                blurRadius: 2,
              ),
            ],
          ),
          child: Column(
            children: [
              _buildSwitchItem(
                context: context,
                icon: Icons.face,
                iconColor: Colors.indigo,
                iconBgColor: Colors.indigo.withValues(alpha: 0.1),
                title: 'Face ID',
                value: _faceId,
                onChanged: (val) => setState(() => _faceId = val),
                showBorder: true,
              ),
              _buildMenuItem(
                context: context,
                icon: Icons.lock,
                iconColor: theme.textTheme.bodyMedium?.color ?? Colors.grey[600]!,
                iconBgColor: theme.dividerColor.withValues(alpha: 0.3),
                title: 'Change Password',
                showBorder: true,
              ),
              _buildMenuItem(
                context: context,
                icon: Icons.policy,
                iconColor: theme.textTheme.bodyMedium?.color ?? Colors.grey[600]!,
                iconBgColor: theme.dividerColor.withValues(alpha: 0.3),
                title: 'Privacy Policy',
                showBorder: false,
                trailing: Icons.open_in_new,
              ),
            ],
          ),
        ),
      ],
    );
  }

  Widget _buildLogoutButton(BuildContext context) {
    return Container(
      width: double.infinity,
      decoration: BoxDecoration(
        color: Colors.red.withValues(alpha: 0.1),
        borderRadius: BorderRadius.circular(16),
      ),
      child: Material(
        color: Colors.transparent,
        child: InkWell(
          onTap: _isLoggingOut ? null : _logout,
          borderRadius: BorderRadius.circular(16),
          child: Padding(
            padding: EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                if (_isLoggingOut)
                  SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.5,
                      color: Colors.red,
                    ),
                  )
                else
                  Icon(Icons.logout, color: Colors.red),
                SizedBox(width: 8),
                Text(
                  _isLoggingOut ? 'Logging Out' : 'Log Out',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 16,
                    fontWeight: FontWeight.w700,
                    color: Colors.red,
                  ),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }

  Future<void> _logout() async {
    setState(() => _isLoggingOut = true);

    try {
      await ref.read(authControllerProvider.notifier).logout();
      // AppRoot reactively navigates to SignInPage when isAuthenticated becomes false.
    } catch (e) {
      if (!mounted) return;
      ScaffoldMessenger.of(context).showSnackBar(
        SnackBar(
          content: Text(e.toString()),
          behavior: SnackBarBehavior.floating,
          backgroundColor: Colors.red,
        ),
      );
    } finally {
      if (mounted) {
        setState(() => _isLoggingOut = false);
      }
    }
  }

  Widget _buildMenuItem({
    required BuildContext context,
    required IconData icon,
    required Color iconColor,
    required Color iconBgColor,
    required String title,
    String? subtitle,
    bool showBorder = true,
    IconData trailing = Icons.chevron_right,
    VoidCallback? onTap,
  }) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: onTap ?? () {},
      child: Container(
        padding: EdgeInsets.all(16),
        decoration: BoxDecoration(
          border: showBorder
              ? Border(bottom: BorderSide(color: theme.dividerColor))
              : null,
        ),
        child: Row(
          children: [
            Container(
              padding: EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: iconBgColor,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: iconColor, size: 24),
            ),
            SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                      color: theme.textTheme.titleMedium?.color,
                    ),
                  ),
                  if (subtitle != null) ...[
                    Text(
                      subtitle,
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 12,
                        color: theme.textTheme.bodySmall?.color,
                      ),
                    ),
                  ],
                ],
              ),
            ),
            Icon(trailing, color: theme.textTheme.bodySmall?.color, size: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildSwitchItem({
    required BuildContext context,
    required IconData icon,
    required Color iconColor,
    required Color iconBgColor,
    required String title,
    required bool value,
    required ValueChanged<bool> onChanged,
    bool showBorder = true,
  }) {
    final theme = Theme.of(context);
    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        border: showBorder
            ? Border(bottom: BorderSide(color: theme.dividerColor))
            : null,
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                padding: EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: iconBgColor,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(icon, color: iconColor, size: 24),
              ),
              SizedBox(width: 12),
              Text(
                title,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  fontWeight: FontWeight.w700,
                  color: theme.textTheme.titleMedium?.color,
                ),
              ),
            ],
          ),
          SizedBox(
            height: 24,
            child: Switch(
              value: value,
              onChanged: onChanged,
              activeTrackColor: theme.colorScheme.primary.withValues(alpha: 0.5),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildThemeModeItem({
    required BuildContext context,
    required ThemeMode value,
    required ValueChanged<ThemeMode> onChanged,
  }) {
    final theme = Theme.of(context);

    return Container(
      padding: EdgeInsets.all(16),
      decoration: BoxDecoration(
        border: Border(bottom: BorderSide(color: theme.dividerColor)),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              Container(
                padding: EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: theme.dividerColor.withValues(alpha: 0.3),
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(
                  Icons.contrast,
                  color: theme.textTheme.bodyMedium?.color ?? Colors.grey[600]!,
                  size: 24,
                ),
              ),
              SizedBox(width: 12),
              Text(
                'Theme',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  fontWeight: FontWeight.w700,
                  color: theme.textTheme.titleMedium?.color,
                ),
              ),
            ],
          ),
          SizedBox(height: 12),
          SegmentedButton<ThemeMode>(
            segments: const [
              ButtonSegment(
                value: ThemeMode.light,
                icon: Icon(Icons.light_mode),
                label: Text('Light'),
              ),
              ButtonSegment(
                value: ThemeMode.dark,
                icon: Icon(Icons.dark_mode),
                label: Text('Dark'),
              ),
              ButtonSegment(
                value: ThemeMode.system,
                icon: Icon(Icons.phone_android),
                label: Text('System'),
              ),
            ],
            selected: {value},
            onSelectionChanged: (selection) => onChanged(selection.first),
            style: ButtonStyle(
              visualDensity: VisualDensity.compact,
              backgroundColor: WidgetStateProperty.resolveWith((states) {
                if (states.contains(WidgetState.selected)) {
                  return theme.colorScheme.primary.withValues(alpha: 0.14);
                }
                return theme.colorScheme.surface;
              }),
              foregroundColor: WidgetStateProperty.resolveWith((states) {
                if (states.contains(WidgetState.selected)) {
                  return theme.colorScheme.primary;
                }
                return theme.textTheme.bodySmall?.color;
              }),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildValueItem({
    required BuildContext context,
    required IconData icon,
    required Color iconColor,
    required Color iconBgColor,
    required String title,
    required String value,
    bool showBorder = true,
  }) {
    final theme = Theme.of(context);
    return InkWell(
      onTap: () {},
      child: Container(
        padding: EdgeInsets.all(16),
        decoration: BoxDecoration(
          border: showBorder
              ? Border(bottom: BorderSide(color: theme.dividerColor))
              : null,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Container(
                  padding: EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: iconBgColor,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(icon, color: iconColor, size: 24),
                ),
                SizedBox(width: 12),
                Text(
                  title,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: theme.textTheme.titleMedium?.color,
                  ),
                ),
              ],
            ),
            Row(
              children: [
                Text(
                  value,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                    color: theme.textTheme.bodySmall?.color,
                  ),
                ),
                Icon(Icons.chevron_right, color: theme.textTheme.bodySmall?.color, size: 20),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
