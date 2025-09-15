import 'package:flutter/material.dart';

enum Category { groceries, food, transport, travel, entertainment, bills, shopping, health, sports, others }

class CategoryIcons {
  static const Map<Category, IconData> _map = {
    Category.groceries: Icons.shopping_cart,
    Category.food: Icons.restaurant,
    Category.transport: Icons.directions_bus,
    Category.travel: Icons.flight,
    Category.entertainment: Icons.movie,
    Category.bills: Icons.receipt_long,
    Category.shopping: Icons.local_mall,
    Category.health: Icons.health_and_safety,
    Category.sports: Icons.sports_soccer,
    Category.others: Icons.category,
  };

  static IconData of(Category c) => _map[c] ?? Icons.category;
}
