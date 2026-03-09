import 'package:flutter/material.dart';

class TransactionDraft {
  final String id;
  final String transactionName;
  final double amount;
  final String type;
  final String? accountId;
  final String? categoryId;
  final DateTime occurredAt;
  final String currency;
  final String notes;

  // Local/UI only fields
  final String originalMessage;
  final bool isChecked;
  final Color? categoryColor;
  final IconData? categoryIcon;

  TransactionDraft({
    required this.id,
    required this.transactionName,
    required this.amount,
    required this.type,
    this.accountId,
    this.categoryId,
    required this.occurredAt,
    this.currency = 'INR',
    this.notes = '',
    required this.originalMessage,
    this.isChecked = false,
    this.categoryColor,
    this.categoryIcon,
  });

  factory TransactionDraft.fromJson(Map<String, dynamic> json) {
    return TransactionDraft(
      id:
          json['id'] as String? ??
          json['uniqueIdentifier'] as String? ??
          '', // Mapping uniqueIdentifier from Android/Backend to id
      transactionName:
          json['transactionName'] as String? ?? 'Unknown Transaction',
      amount: (json['amount'] as num?)?.toDouble() ?? 0.0,
      type: json['type'] as String? ?? 'EXPENSE',
      accountId: json['accountId'] as String? ?? json['account'] as String?,
      categoryId: json['categoryId'] as String? ?? json['category'] as String?,
      occurredAt:
          DateTime.tryParse(json['occurredAt'] as String? ?? '') ??
          DateTime.now(),
      currency: json['currency'] as String? ?? 'INR',
      notes: json['notes'] as String? ?? '',
      originalMessage: json['originalMessage'] as String? ?? json['body'] ?? '',
      isChecked: false, // Default to unchecked
    );
  }

  TransactionDraft copyWith({
    String? id,
    String? transactionName,
    double? amount,
    String? type,
    String? account,
    String? category,
    DateTime? occurredAt,
    String? currency,
    String? notes,
    String? originalMessage,
    bool? isChecked,
    Color? categoryColor,
    IconData? categoryIcon,
    required String categoryId,
    required String accountId,
  }) {
    return TransactionDraft(
      id: id ?? this.id,
      transactionName: transactionName ?? this.transactionName,
      amount: amount ?? this.amount,
      type: type ?? this.type,
      accountId: accountId,
      categoryId: categoryId,
      occurredAt: occurredAt ?? this.occurredAt,
      currency: currency ?? this.currency,
      notes: notes ?? this.notes,
      originalMessage: originalMessage ?? this.originalMessage,
      isChecked: isChecked ?? this.isChecked,
      categoryColor: categoryColor ?? this.categoryColor,
      categoryIcon: categoryIcon ?? this.categoryIcon,
    );
  }
}
