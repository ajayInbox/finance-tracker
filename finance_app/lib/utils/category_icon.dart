import 'package:flutter/material.dart';

// enum Category { groceries, food, transport, travel, entertainment, bills, shopping, health, sports, others }

class CategoryIcons {
  static const Map<String, IconData> _map = {
    "groceries": Icons.shopping_cart,
    "food": Icons.restaurant,
    "transport": Icons.directions_bus,
    "travel": Icons.flight,
    "entertainment": Icons.movie,
    "bills": Icons.receipt_long,
    "shopping": Icons.local_mall,
    "health": Icons.health_and_safety,
    "sports": Icons.sports_soccer,
    "others": Icons.category,
  };

  static IconData of(String c) => _map[c] ?? Icons.category;

  static IconData parseIcon(String iconKey) {
    if (iconKey.isEmpty) return Icons.category;
    
    List<String> iconParts = iconKey.split('+');
    if (iconParts.length == 2) {
      int? codePoint = int.tryParse(iconParts[0]);
      String fontFamily = iconParts[1];
      if (codePoint != null) {
        return IconData(
          codePoint,
          fontFamily: fontFamily,
          fontPackage: fontFamily == 'CupertinoIcons' ? 'cupertino_icons' : null,
        );
      }
    }
    return Icons.category;
  }

  static Color parseColor(String colorCode, {Color fallback = const Color(0xFF6B7280)}) {
    if (colorCode.isEmpty) return fallback;
    try {
      String hex = colorCode.replaceAll('#', '');
      if (hex.length == 6) {
        hex = 'FF$hex';
      }
      return Color(int.parse(hex, radix: 16));
    } catch (e) {
      return fallback;
    }
  }
}
