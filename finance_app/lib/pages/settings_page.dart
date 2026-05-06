import 'package:finance_app/features/category/ui/category_management_page.dart';
import 'package:finance_app/features/auth/application/auth_controller.dart';
import 'package:finance_app/features/auth/data/model/user_profile.dart';
import 'package:finance_app/features/auth/data/providers/user_profile_provider.dart';
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
  bool _darkMode = false;
  bool _faceId = false;
  bool _isLoggingOut = false;

  // App version
  final String _appVersion = '2.4.1 (Build 202)';

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: const Color(0xFFF6F8F6), // background-light from MD
      body: SingleChildScrollView(
        padding: const EdgeInsets.only(bottom: 100),
        child: Column(
          children: [
            const AppPageHeader(title: 'Settings'),
            Padding(
              padding: const EdgeInsets.symmetric(horizontal: 16), // px-4
              child: Column(
                children: [
                  _buildProfileSection(),
                  const SizedBox(height: 24),
                  _buildFinancesSection(),
                  const SizedBox(height: 24),
                  _buildPreferencesSection(),
                  const SizedBox(height: 24),
                  _buildSecuritySection(),
                  const SizedBox(height: 8),
                  _buildLogoutButton(),
                  const SizedBox(height: 8),
                  Text(
                    'App Version $_appVersion',
                    textAlign: TextAlign.center,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 12,
                      color: Colors.grey[400],
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

  Widget _buildProfileSection() {
    final profileAsync = ref.watch(userProfileProvider);

    return profileAsync.when(
      data: (user) => _buildProfileContent(user),
      loading: () => _buildProfileShimmer(),
      error: (e, _) => _buildProfileError(),
    );
  }

  Widget _buildProfileContent(UserProfile user) {
    return Column(
      children: [
        // ── Initials Avatar ──
        Container(
          width: 96,
          height: 96,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 4),
            boxShadow: const [
              BoxShadow(color: Colors.black12, blurRadius: 10),
            ],
            gradient: const LinearGradient(
              begin: Alignment.topLeft,
              end: Alignment.bottomRight,
              colors: [Color(0xFF10B981), Color(0xFF059669)],
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
        const SizedBox(height: 16),
        Text(
          user.name,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 20,
            fontWeight: FontWeight.w700,
            color: const Color(0xFF111827),
          ),
        ),
        const SizedBox(height: 2),
        Text(
          user.email,
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            color: Colors.grey[500],
          ),
        ),
        if (user.isSubscribed) ...[
          const SizedBox(height: 16),
          Container(
            padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
            decoration: BoxDecoration(
              color: const Color(0xFF13EC5B).withValues(alpha: 0.15),
              borderRadius: BorderRadius.circular(999),
              border: Border.all(
                color: const Color(0xFF13EC5B).withValues(alpha: 0.3),
              ),
            ),
            child: Row(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(
                  Icons.workspace_premium,
                  size: 14,
                  color: Color(0xFF13EC5B),
                ),
                const SizedBox(width: 4),
                Text(
                  'Pro Member',
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 12,
                    fontWeight: FontWeight.w700,
                    color: const Color(0xFF13EC5B),
                  ),
                ),
              ],
            ),
          ),
        ],
      ],
    );
  }

  Widget _buildProfileShimmer() {
    return Shimmer.fromColors(
      baseColor: Colors.grey[300]!,
      highlightColor: Colors.grey[100]!,
      child: Column(
        children: [
          // Avatar placeholder
          Container(
            width: 96,
            height: 96,
            decoration: const BoxDecoration(
              shape: BoxShape.circle,
              color: Colors.white,
            ),
          ),
          const SizedBox(height: 16),
          // Name placeholder
          Container(
            width: 140,
            height: 20,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(4),
            ),
          ),
          const SizedBox(height: 8),
          // Email placeholder
          Container(
            width: 200,
            height: 14,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(4),
            ),
          ),
          const SizedBox(height: 16),
          // Badge placeholder
          Container(
            width: 100,
            height: 24,
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(999),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildProfileError() {
    return Column(
      children: [
        // Fallback avatar with question mark
        Container(
          width: 96,
          height: 96,
          decoration: BoxDecoration(
            shape: BoxShape.circle,
            border: Border.all(color: Colors.white, width: 4),
            boxShadow: const [
              BoxShadow(color: Colors.black12, blurRadius: 10),
            ],
            color: Colors.grey[200],
          ),
          child: Center(
            child: Icon(
              Icons.person_outline_rounded,
              size: 40,
              color: Colors.grey[400],
            ),
          ),
        ),
        const SizedBox(height: 16),
        Text(
          'Couldn\'t load profile',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 14,
            color: Colors.grey[500],
          ),
        ),
        const SizedBox(height: 8),
        GestureDetector(
          onTap: () => ref.invalidate(userProfileProvider),
          child: Text(
            'Tap to retry',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 13,
              fontWeight: FontWeight.w600,
              color: const Color(0xFF10B981),
            ),
          ),
        ),
      ],
    );
  }

  Widget _buildFinancesSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 8, bottom: 8),
          child: Text(
            'FINANCES',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: Colors.grey[500],
              letterSpacing: 1.0,
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16), // rounded-2xl
            border: Border.all(color: Colors.grey[100]!),
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
                icon: Icons.data_usage,
                iconColor: Colors.orange,
                iconBgColor: Colors.orange.withValues(alpha: 0.1),
                title: 'Spending Limits',
                showBorder: true,
                onTap: () {
                  ScaffoldMessenger.of(context).showSnackBar(
                    SnackBar(
                      content: const Text('Coming soon!'),
                      behavior: SnackBarBehavior.floating,
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                      backgroundColor: const Color(0xFF10B981),
                      duration: const Duration(seconds: 2),
                    ),
                  );
                },
              ),
              _buildMenuItem(
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

  Widget _buildPreferencesSection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 8, bottom: 8),
          child: Text(
            'APP PREFERENCES',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: Colors.grey[500],
              letterSpacing: 1.0,
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Colors.grey[100]!),
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
                icon: Icons.dark_mode,
                iconColor: Colors.grey[600]!,
                iconBgColor: Colors.grey.withValues(alpha: 0.1),
                title: 'Dark Mode',
                value: _darkMode,
                onChanged: (val) => setState(() => _darkMode = val),
                showBorder: true,
              ),
              _buildValueItem(
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

  Widget _buildSecuritySection() {
    return Column(
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Padding(
          padding: const EdgeInsets.only(left: 8, bottom: 8),
          child: Text(
            'SECURITY',
            style: GoogleFonts.plusJakartaSans(
              fontSize: 12,
              fontWeight: FontWeight.w700,
              color: Colors.grey[500],
              letterSpacing: 1.0,
            ),
          ),
        ),
        Container(
          decoration: BoxDecoration(
            color: Colors.white,
            borderRadius: BorderRadius.circular(16),
            border: Border.all(color: Colors.grey[100]!),
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
                icon: Icons.face,
                iconColor: Colors.indigo,
                iconBgColor: Colors.indigo.withValues(alpha: 0.1),
                title: 'Face ID',
                value: _faceId,
                onChanged: (val) => setState(() => _faceId = val),
                showBorder: true,
              ),
              _buildMenuItem(
                icon: Icons.lock,
                iconColor: Colors.grey[600]!,
                iconBgColor: Colors.grey.withValues(alpha: 0.1),
                title: 'Change Password',
                showBorder: true,
              ),
              _buildMenuItem(
                icon: Icons.policy,
                iconColor: Colors.grey[600]!,
                iconBgColor: Colors.grey.withValues(alpha: 0.1),
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

  Widget _buildLogoutButton() {
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
            padding: const EdgeInsets.all(16),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                if (_isLoggingOut)
                  const SizedBox(
                    width: 20,
                    height: 20,
                    child: CircularProgressIndicator(
                      strokeWidth: 2.5,
                      color: Colors.red,
                    ),
                  )
                else
                  const Icon(Icons.logout, color: Colors.red),
                const SizedBox(width: 8),
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
    required IconData icon,
    required Color iconColor,
    required Color iconBgColor,
    required String title,
    String? subtitle,
    bool showBorder = true,
    IconData trailing = Icons.chevron_right,
    VoidCallback? onTap,
  }) {
    return InkWell(
      onTap: onTap ?? () {},
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          border: showBorder
              ? Border(bottom: BorderSide(color: Colors.grey[100]!))
              : null,
        ),
        child: Row(
          children: [
            Container(
              padding: const EdgeInsets.all(8),
              decoration: BoxDecoration(
                color: iconBgColor,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Icon(icon, color: iconColor, size: 24),
            ),
            const SizedBox(width: 12),
            Expanded(
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(
                    title,
                    style: GoogleFonts.plusJakartaSans(
                      fontSize: 14,
                      fontWeight: FontWeight.w700,
                      color: const Color(0xFF111827),
                    ),
                  ),
                  if (subtitle != null) ...[
                    Text(
                      subtitle,
                      style: GoogleFonts.plusJakartaSans(
                        fontSize: 12,
                        color: Colors.grey[500],
                      ),
                    ),
                  ],
                ],
              ),
            ),
            Icon(trailing, color: Colors.grey[400], size: 20),
          ],
        ),
      ),
    );
  }

  Widget _buildSwitchItem({
    required IconData icon,
    required Color iconColor,
    required Color iconBgColor,
    required String title,
    required bool value,
    required ValueChanged<bool> onChanged,
    bool showBorder = true,
  }) {
    return Container(
      padding: const EdgeInsets.all(16),
      decoration: BoxDecoration(
        border: showBorder
            ? Border(bottom: BorderSide(color: Colors.grey[100]!))
            : null,
      ),
      child: Row(
        mainAxisAlignment: MainAxisAlignment.spaceBetween,
        children: [
          Row(
            children: [
              Container(
                padding: const EdgeInsets.all(8),
                decoration: BoxDecoration(
                  color: iconBgColor,
                  borderRadius: BorderRadius.circular(8),
                ),
                child: Icon(icon, color: iconColor, size: 24),
              ),
              const SizedBox(width: 12),
              Text(
                title,
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 14,
                  fontWeight: FontWeight.w700,
                  color: const Color(0xFF111827),
                ),
              ),
            ],
          ),
          SizedBox(
            height: 24,
            child: Switch(
              value: value,
              onChanged: onChanged,
              activeColor: const Color(0xFF13EC5B), // primary
              activeTrackColor: const Color(0xFF13EC5B).withValues(alpha: 0.2),
            ),
          ),
        ],
      ),
    );
  }

  Widget _buildValueItem({
    required IconData icon,
    required Color iconColor,
    required Color iconBgColor,
    required String title,
    required String value,
    bool showBorder = true,
  }) {
    return InkWell(
      onTap: () {},
      child: Container(
        padding: const EdgeInsets.all(16),
        decoration: BoxDecoration(
          border: showBorder
              ? Border(bottom: BorderSide(color: Colors.grey[100]!))
              : null,
        ),
        child: Row(
          mainAxisAlignment: MainAxisAlignment.spaceBetween,
          children: [
            Row(
              children: [
                Container(
                  padding: const EdgeInsets.all(8),
                  decoration: BoxDecoration(
                    color: iconBgColor,
                    borderRadius: BorderRadius.circular(8),
                  ),
                  child: Icon(icon, color: iconColor, size: 24),
                ),
                const SizedBox(width: 12),
                Text(
                  title,
                  style: GoogleFonts.plusJakartaSans(
                    fontSize: 14,
                    fontWeight: FontWeight.w700,
                    color: const Color(0xFF111827),
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
                    color: Colors.grey[500],
                  ),
                ),
                Icon(Icons.chevron_right, color: Colors.grey[400], size: 20),
              ],
            ),
          ],
        ),
      ),
    );
  }
}
