// lib/main.dart
import 'package:flutter/material.dart';
import 'package:google_fonts/google_fonts.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:finance_app/features/auth/application/auth_controller.dart';
import 'package:finance_app/features/auth/ui/landing_page.dart';
import 'package:finance_app/features/auth/ui/sign_in_page.dart';
import 'package:finance_app/features/auth/ui/sign_up_page.dart';
import 'package:finance_app/features/transaction/ui/transactions_page.dart';
import 'package:finance_app/pages/dashboard_page.dart';
import 'package:finance_app/features/account/ui/accounts_page.dart';
import 'package:finance_app/features/sms/ui/sms_review_page.dart';
import 'package:finance_app/pages/settings_page.dart';


import 'package:finance_app/core/theme_provider.dart';
import 'package:finance_app/utils/app_style_constants.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  final initialThemeMode = await loadSavedThemeMode();

  runApp(
    ProviderScope(
      overrides: [
        initialThemeModeProvider.overrideWithValue(initialThemeMode),
      ],
      child: const MyApp(),
    ),
  );
}

class MyApp extends ConsumerWidget {
  const MyApp({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final themeMode = ref.watch(themeProvider);

    return MaterialApp(
      title: 'Finance Tracker',
      debugShowCheckedModeBanner: false,
      themeMode: themeMode,
      theme: AppTheme.lightTheme,
      darkTheme: AppTheme.darkTheme,
      home: const AppRoot(),
    );
  }
}

/// Controls whether the auth flow shows SignInPage or SignUpPage.
final showSignUpProvider = NotifierProvider<ShowSignUpNotifier, bool>(
  ShowSignUpNotifier.new,
);

class ShowSignUpNotifier extends Notifier<bool> {
  @override
  bool build() => false;

  void show() => state = true;
  void hide() => state = false;
}

class AppRoot extends ConsumerWidget {
  const AppRoot({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final authState = ref.watch(authControllerProvider);
    final showSignUp = ref.watch(showSignUpProvider);

    return authState.when(
      loading: () => const _StartupLoadingPage(),
      error: (error, stackTrace) => _StartupErrorPage(
        onRetry: () => ref.invalidate(authControllerProvider),
      ),
      data: (state) {
        if (state.isAuthenticated) {
          return const MyHomePage(title: 'Finance Tracker');
        }

        if (!state.hasSeenLanding) {
          return const LandingPage();
        }

        return showSignUp ? const SignUpPage() : const SignInPage();
      },
    );
  }
}

class _StartupLoadingPage extends StatelessWidget {
  const _StartupLoadingPage();

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: Center(
        child: CircularProgressIndicator(
          color: Theme.of(context).colorScheme.primary,
        ),
      ),
    );
  }
}

class _StartupErrorPage extends StatelessWidget {
  const _StartupErrorPage({required this.onRetry});

  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    final theme = Theme.of(context);

    return Scaffold(
      backgroundColor: theme.scaffoldBackgroundColor,
      body: Center(
        child: Padding(
          padding: const EdgeInsets.symmetric(horizontal: 32),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Container(
                width: 72,
                height: 72,
                decoration: BoxDecoration(
                  color: const Color(0xFFEF4444).withValues(alpha: 0.1),
                  shape: BoxShape.circle,
                ),
                child: const Icon(
                  Icons.error_outline_rounded,
                  color: Color(0xFFEF4444),
                  size: 36,
                ),
              ),
              const SizedBox(height: 24),
              Text(
                'Something went wrong',
                style: GoogleFonts.plusJakartaSans(
                  fontSize: 20,
                  fontWeight: FontWeight.w700,
                  color: theme.textTheme.titleLarge?.color,
                ),
              ),
              const SizedBox(height: 8),
              Text(
                'We couldn\'t start the app. Please try again.',
                textAlign: TextAlign.center,
                style: GoogleFonts.inter(
                  fontSize: 14,
                  color: theme.textTheme.bodySmall?.color,
                  height: 1.5,
                ),
              ),
              const SizedBox(height: 28),
              SizedBox(
                width: 160,
                height: 48,
                child: FilledButton.icon(
                  onPressed: onRetry,
                  icon: const Icon(Icons.refresh_rounded, size: 20),
                  label: const Text('Try Again'),
                  style: FilledButton.styleFrom(
                    backgroundColor: theme.colorScheme.primary,
                    foregroundColor: theme.colorScheme.onPrimary,
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(14),
                    ),
                    textStyle: GoogleFonts.plusJakartaSans(
                      fontSize: 15,
                      fontWeight: FontWeight.w700,
                    ),
                  ),
                ),
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class MyHomePage extends StatefulWidget {
  const MyHomePage({super.key, required this.title});

  final String title;

  @override
  State<MyHomePage> createState() => _MyHomePageState();
}

class _MyHomePageState extends State<MyHomePage> {
  int _selectedIndex = 0;

  final List<Widget> _widgetOptions = <Widget>[
    DashboardPage(),
    TransactionsPage(),
    AccountsPage(),
    SmsReviewPage(), // New Page
    SettingsPage(),
  ];

  @override
  void initState() {
    super.initState();
  }

  void _onItemTapped(int index) {
    setState(() {
      _selectedIndex = index;
    });
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      backgroundColor: Theme.of(context).scaffoldBackgroundColor,
      body: Stack(
        children: [
          // Content
          Padding(
            padding: const EdgeInsets.only(
              top: 0,
              bottom: 80,
            ), // Reserve space for nav only
            child: _widgetOptions[_selectedIndex],
          ),

          // Bottom Nav
          Positioned(
            left: 0,
            right: 0,
            bottom: 0,
            child: _buildFloatingBottomBar(context),
          ),
        ],
      ),
    );
  }

  Widget _buildFloatingBottomBar(BuildContext context) {
    final theme = Theme.of(context);
    return Container(
      padding: const EdgeInsets.fromLTRB(24, 0, 24, 24),
      child: Center(
        child: Container(
          constraints: const BoxConstraints(maxWidth: 450), // Match max-w-md
          child: Container(
            height: 80,
            decoration: BoxDecoration(
              color: theme.bottomNavigationBarTheme.backgroundColor,
              borderRadius: BorderRadius.circular(40),
              boxShadow: [
                BoxShadow(
                  color: Colors.black.withValues(alpha: 0.1),
                  blurRadius: 40,
                  offset: const Offset(0, -10),
                ),
              ],
            ),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceEvenly,
              children: [
                _buildNavItem(context, Icons.home, 0),
                _buildNavItem(context, Icons.history, 1),
                _buildNavItem(context, Icons.donut_large, 2),
                _buildNavItem(context, Icons.sms, 3), // SMS Icon
                _buildNavItem(context, Icons.settings, 4), // Settings shifts to 4
              ],
            ),
          ),
        ),
      ),
    );
  }

  Widget _buildNavItem(BuildContext context, IconData icon, int index) {
    bool isSelected = _selectedIndex == index;
    final theme = Theme.of(context);
    return GestureDetector(
      onTap: () => _onItemTapped(index),
      behavior: HitTestBehavior.opaque,
      child: Container(
        padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
        child: Column(
          mainAxisSize: MainAxisSize.min,
          mainAxisAlignment: MainAxisAlignment.center,
          children: [
            Icon(
              icon,
              color: isSelected ? theme.bottomNavigationBarTheme.selectedItemColor : theme.bottomNavigationBarTheme.unselectedItemColor,
              size: 28,
            ),
            const SizedBox(height: 4),
            AnimatedContainer(
              duration: const Duration(milliseconds: 200),
              width: isSelected ? 4 : 0,
              height: 4,
              decoration: BoxDecoration(
                color: theme.bottomNavigationBarTheme.selectedItemColor,
                shape: BoxShape.circle,
              ),
            ),
          ],
        ),
      ),
    );
  }
}
