import 'package:flutter/material.dart';

enum Category { groceries, food, transport, travel, entertainment, bills, shopping, health, sports, others }

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
}
