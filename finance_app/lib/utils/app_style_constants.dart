
import 'package:flutter/material.dart';

// Design System Colors
class AppColors {
  // Primary Colors
  static const Color primaryBlue = Color(0xFF1E40AF);
  static const Color primaryGradientStart = Color(0xFF1E40AF);
  static const Color primaryGradientEnd = Color(0xFF7C3AED);
  static const Color secondaryTeal = Color(0xFF0D9488);

  // Account Type Colors
  static const Color typeBank = Color(0xFF3B82F6);
  static const Color typeCard = Color(0xFFF97316);
  static const Color typeCash = Color(0xFF10B981);
  static const Color typeInvestment = Color(0xFF8B5CF6);
  static const Color typeLoan = Color(0xFFEF4444);
  static const Color typeWallet = Color(0xFF06B6D4);

  // Semantic Colors
  static const Color success = Color(0xFF10B981);
  static const Color successLight = Color(0xFFD1FAE5);
  static const Color successBorder = Color(0xFF059669);
  static const Color error = Color(0xFFEF4444);
  static const Color errorLight = Color(0xFFFEE2E2);
  static const Color errorBorder = Color(0xFFDC2626);
  static const Color warning = Color(0xFFF59E0B);
  static const Color info = Color(0xFF3B82F6);

  // Neutrals - Light Mode
  static const Color background = Color(0xFFF9FAFB);
  static const Color surface = Color(0xFFFFFFFF);
  static const Color border = Color(0xFFE5E7EB);
  static const Color borderFocus = Color(0xFF1E40AF);
  static const Color textPrimary = Color(0xFF111827);
  static const Color textSecondary = Color(0xFF6B7280);
  static const Color textTertiary = Color(0xFF9CA3AF);
  static const Color textPlaceholder = Color(0xFFD1D5DB);

  // Background colors
  static const Color lightBackground = Color(0xFFF9FAFB);
  static const Color darkBackground = Color(0xFF111827);
  static const Color lightSurface = Colors.white;
  static const Color darkSurface = Color(0xFF1F2937);

  // Text colors
  static const Color lightTextPrimary = Color(0xFF111827);
  static const Color darkTextPrimary = Color(0xFFF9FAFB);
  static const Color cardBorder = Color(0xFFE5E7EB);
  static const Color textMuted = Color(0xFF6F6F7A);

}

// Typography Scale
class AppTypography {
  static const String fontFamilyBase = '-apple-system, BlinkMacSystemFont, Segoe UI, Roboto, Helvetica Neue, Arial, sans-serif';
  static const String fontFamilyNumbers = 'SF Mono, Roboto Mono, Courier New, monospace';

  static const double textXs = 11;
  static const double textSm = 12;
  static const double textBase = 13;
  static const double textMd = 14;
  static const double textLg = 16;
  static const double textXl = 18;
  static const double text2xl = 20;
  static const double text3xl = 24;

  static const FontWeight weightRegular = FontWeight.w400;
  static const FontWeight weightMedium = FontWeight.w500;
  static const FontWeight weightSemibold = FontWeight.w600;
  static const FontWeight weightBold = FontWeight.w700;

  static const double lineHeightTight = 1.2;
  static const double lineHeightNormal = 1.5;
  static const double lineHeightRelaxed = 1.7;

  static const double h1 = 36.0; // Net Worth
  static const double h2 = 20.0; // Section titles
  static const double h3 = 16.0; // Account names
  static const double body = 14.0;
  static const double caption = 12.0;
}

// Spacing System
class AppSpacing {
  static const double space1 = 4;
  static const double space2 = 8;
  static const double space3 = 12;
  static const double space4 = 16;
  static const double space5 = 20;
  static const double space6 = 24;
  static const double space8 = 32;
  static const double space10 = 40;
  static const double space12 = 48;
}

// Border Radius
class AppRadius {
  static const double radiusSm = 8;
  static const double radiusMd = 10;
  static const double radiusLg = 12;
  static const double radiusXl = 16;
  static const double radius2xl = 20;
  static const double radiusFull = 9999;
}

// Shadows
class AppShadows {
  static const List<BoxShadow> shadowSm = [
    BoxShadow(color: Color.fromRGBO(0, 0, 0, 0.05), offset: Offset(0, 1), blurRadius: 2)
  ];
  static const List<BoxShadow> shadowMd = [
    BoxShadow(color: Color.fromRGBO(0, 0, 0, 0.1), offset: Offset(0, 4), blurRadius: 6, spreadRadius: -1)
  ];
  static const List<BoxShadow> shadowLg = [
    BoxShadow(color: Color.fromRGBO(0, 0, 0, 0.1), offset: Offset(0, 10), blurRadius: 15, spreadRadius: -3)
  ];
  static const List<BoxShadow> shadowXl = [
    BoxShadow(color: Color.fromRGBO(0, 0, 0, 0.1), offset: Offset(0, 20), blurRadius: 25, spreadRadius: -5)
  ];
  static const List<BoxShadow> shadowFocus = [
    BoxShadow(color: Color.fromRGBO(30, 64, 175, 0.1), offset: Offset(0, 0), blurRadius: 0, spreadRadius: 3)
  ];
  static const List<BoxShadow> shadowButton = [
    BoxShadow(color: Color.fromRGBO(30, 64, 175, 0.2), offset: Offset(0, 4), blurRadius: 12)
  ];
}

class AppBorderRadius {
  static const double small = 8.0;
  static const double medium = 12.0;
  static const double large = 16.0;
  static const double xlarge = 24.0;
}