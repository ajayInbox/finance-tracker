import 'package:finance_app/features/auth/application/auth_controller.dart';
import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:google_fonts/google_fonts.dart';

class LandingPage extends ConsumerWidget {
  const LandingPage({super.key});

  Future<void> _getStarted(WidgetRef ref) async {
    await ref.read(authControllerProvider.notifier).markLandingSeen();
  }

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      backgroundColor: const Color(0xFFF7FAF9),
      body: SafeArea(
        child: LayoutBuilder(
          builder: (context, constraints) {
            final isWide = constraints.maxWidth > 720;
            return SingleChildScrollView(
              child: ConstrainedBox(
                constraints: BoxConstraints(minHeight: constraints.maxHeight),
                child: Padding(
                  padding: const EdgeInsets.fromLTRB(24, 18, 24, 24),
                  child: Center(
                    child: ConstrainedBox(
                      constraints: const BoxConstraints(maxWidth: 980),
                      child: isWide
                          ? Row(
                              crossAxisAlignment: CrossAxisAlignment.center,
                              children: [
                                Expanded(
                                  flex: 6,
                                  child: _LandingCopy(
                                    onGetStarted: () => _getStarted(ref),
                                  ),
                                ),
                                const SizedBox(width: 40),
                                const Expanded(
                                  flex: 5,
                                  child: _InsightPreview(),
                                ),
                              ],
                            )
                          : Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                _LandingCopy(
                                  onGetStarted: () => _getStarted(ref),
                                ),
                                const SizedBox(height: 28),
                                const Center(child: _InsightPreview()),
                              ],
                            ),
                    ),
                  ),
                ),
              ),
            );
          },
        ),
      ),
    );
  }
}

class _LandingCopy extends StatelessWidget {
  const _LandingCopy({required this.onGetStarted});

  final VoidCallback onGetStarted;

  @override
  Widget build(BuildContext context) {
    return Column(
      mainAxisSize: MainAxisSize.min,
      crossAxisAlignment: CrossAxisAlignment.start,
      children: [
        Row(
          children: [
            Container(
              width: 42,
              height: 42,
              decoration: BoxDecoration(
                color: const Color(0xFF0F172A),
                borderRadius: BorderRadius.circular(12),
              ),
              child: const Icon(Icons.sms_outlined, color: Color(0xFF34D399)),
            ),
            const SizedBox(width: 12),
            Text(
              'Finance Tracker',
              style: GoogleFonts.plusJakartaSans(
                fontSize: 20,
                fontWeight: FontWeight.w800,
                color: const Color(0xFF102216),
              ),
            ),
          ],
        ),
        const SizedBox(height: 42),
        Text(
          'Your spending, rebuilt from everyday messages.',
          style: GoogleFonts.plusJakartaSans(
            fontSize: 44,
            fontWeight: FontWeight.w800,
            height: 1.08,
            color: const Color(0xFF0F172A),
          ),
        ),
        const SizedBox(height: 18),
        Text(
          'Turn bank SMS alerts into clear transactions, categories, accounts, and net worth snapshots without rebuilding your money life by hand.',
          style: GoogleFonts.inter(
            fontSize: 16,
            height: 1.55,
            color: const Color(0xFF475569),
          ),
        ),
        const SizedBox(height: 28),
        Wrap(
          spacing: 12,
          runSpacing: 12,
          children: const [
            _FeaturePill(icon: Icons.auto_awesome, label: 'SMS import'),
            _FeaturePill(icon: Icons.donut_large, label: 'Smart categories'),
            _FeaturePill(
              icon: Icons.account_balance_wallet,
              label: 'Net worth',
            ),
          ],
        ),
        const SizedBox(height: 36),
        SizedBox(
          height: 56,
          child: FilledButton.icon(
            onPressed: onGetStarted,
            icon: const Icon(Icons.arrow_forward_rounded),
            label: const Text('Get Started'),
            style: FilledButton.styleFrom(
              backgroundColor: const Color(0xFF10B981),
              foregroundColor: Colors.white,
              padding: const EdgeInsets.symmetric(horizontal: 22),
              shape: RoundedRectangleBorder(
                borderRadius: BorderRadius.circular(16),
              ),
              textStyle: GoogleFonts.plusJakartaSans(
                fontSize: 16,
                fontWeight: FontWeight.w800,
              ),
            ),
          ),
        ),
      ],
    );
  }
}

class _FeaturePill extends StatelessWidget {
  const _FeaturePill({required this.icon, required this.label});

  final IconData icon;
  final String label;

  @override
  Widget build(BuildContext context) {
    return Container(
      padding: const EdgeInsets.symmetric(horizontal: 12, vertical: 10),
      decoration: BoxDecoration(
        color: Colors.white,
        borderRadius: BorderRadius.circular(14),
        border: Border.all(color: const Color(0xFFE2E8F0)),
      ),
      child: Row(
        mainAxisSize: MainAxisSize.min,
        children: [
          Icon(icon, size: 18, color: const Color(0xFF10B981)),
          const SizedBox(width: 8),
          Text(
            label,
            style: GoogleFonts.inter(
              fontSize: 13,
              fontWeight: FontWeight.w700,
              color: const Color(0xFF334155),
            ),
          ),
        ],
      ),
    );
  }
}

class _InsightPreview extends StatelessWidget {
  const _InsightPreview();

  @override
  Widget build(BuildContext context) {
    return Container(
      constraints: const BoxConstraints(maxWidth: 420),
      padding: const EdgeInsets.all(18),
      decoration: BoxDecoration(
        color: const Color(0xFF0F172A),
        borderRadius: BorderRadius.circular(28),
        boxShadow: [
          BoxShadow(
            color: const Color(0xFF0F172A).withValues(alpha: 0.18),
            blurRadius: 40,
            offset: const Offset(0, 24),
          ),
        ],
      ),
      child: Column(
        mainAxisSize: MainAxisSize.min,
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Row(
            children: [
              const Icon(
                Icons.lock_outline,
                color: Color(0xFF94A3B8),
                size: 18,
              ),
              const SizedBox(width: 8),
              Text(
                'Today from SMS',
                style: GoogleFonts.inter(
                  color: const Color(0xFFCBD5E1),
                  fontWeight: FontWeight.w700,
                ),
              ),
            ],
          ),
          const SizedBox(height: 20),
          const _SmsBubble(
            sender: 'HDFC Bank',
            body: 'Rs. 620 spent at FreshMart on card ending 4821.',
          ),
          const SizedBox(height: 10),
          const _SmsBubble(
            sender: 'UPI Alert',
            body: 'Paid Rs. 249 to Metro Transit from savings.',
          ),
          const SizedBox(height: 18),
          Container(
            padding: const EdgeInsets.all(16),
            decoration: BoxDecoration(
              color: Colors.white,
              borderRadius: BorderRadius.circular(20),
            ),
            child: Column(
              children: [
                _InsightRow(
                  icon: Icons.shopping_bag_outlined,
                  title: 'FreshMart',
                  subtitle: 'Groceries',
                  amount: '-Rs. 620',
                  color: const Color(0xFF10B981),
                ),
                const SizedBox(height: 14),
                _InsightRow(
                  icon: Icons.train_outlined,
                  title: 'Metro Transit',
                  subtitle: 'Transport',
                  amount: '-Rs. 249',
                  color: const Color(0xFF3B82F6),
                ),
                const SizedBox(height: 16),
                Container(
                  width: double.infinity,
                  padding: const EdgeInsets.all(14),
                  decoration: BoxDecoration(
                    color: const Color(0xFFF1F5F9),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Text(
                    'Auto-categorized and ready for review',
                    style: GoogleFonts.inter(
                      color: const Color(0xFF334155),
                      fontSize: 13,
                      fontWeight: FontWeight.w800,
                    ),
                  ),
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

class _SmsBubble extends StatelessWidget {
  const _SmsBubble({required this.sender, required this.body});

  final String sender;
  final String body;

  @override
  Widget build(BuildContext context) {
    return Container(
      width: double.infinity,
      padding: const EdgeInsets.all(14),
      decoration: BoxDecoration(
        color: const Color(0xFF1E293B),
        borderRadius: BorderRadius.circular(18),
      ),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          Text(
            sender,
            style: GoogleFonts.inter(
              color: const Color(0xFF34D399),
              fontSize: 12,
              fontWeight: FontWeight.w800,
            ),
          ),
          const SizedBox(height: 5),
          Text(
            body,
            style: GoogleFonts.inter(
              color: const Color(0xFFE2E8F0),
              fontSize: 13,
              height: 1.35,
            ),
          ),
        ],
      ),
    );
  }
}

class _InsightRow extends StatelessWidget {
  const _InsightRow({
    required this.icon,
    required this.title,
    required this.subtitle,
    required this.amount,
    required this.color,
  });

  final IconData icon;
  final String title;
  final String subtitle;
  final String amount;
  final Color color;

  @override
  Widget build(BuildContext context) {
    return Row(
      children: [
        Container(
          width: 40,
          height: 40,
          decoration: BoxDecoration(
            color: color.withValues(alpha: 0.12),
            borderRadius: BorderRadius.circular(12),
          ),
          child: Icon(icon, color: color, size: 21),
        ),
        const SizedBox(width: 12),
        Expanded(
          child: Column(
            crossAxisAlignment: CrossAxisAlignment.start,
            children: [
              Text(
                title,
                style: GoogleFonts.inter(
                  color: const Color(0xFF0F172A),
                  fontWeight: FontWeight.w800,
                ),
              ),
              Text(
                subtitle,
                style: GoogleFonts.inter(
                  color: const Color(0xFF64748B),
                  fontSize: 12,
                ),
              ),
            ],
          ),
        ),
        Text(
          amount,
          style: GoogleFonts.inter(
            color: const Color(0xFFEF4444),
            fontWeight: FontWeight.w800,
          ),
        ),
      ],
    );
  }
}
