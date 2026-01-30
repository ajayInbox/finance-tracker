import 'package:flutter/material.dart';

class TransactionDraft {
  final String id;
  final String merchant;
  final DateTime date;
  final double amount;
  final String originalMessage;
  final bool isChecked;
  final String? assignedCategory;
  final Color? categoryColor;
  final IconData? categoryIcon;

  TransactionDraft({
    required this.id,
    required this.merchant,
    required this.date,
    required this.amount,
    required this.originalMessage,
    this.isChecked = false,
    this.assignedCategory,
    this.categoryColor,
    this.categoryIcon,
  });

  TransactionDraft copyWith({
    String? id,
    String? merchant,
    DateTime? date,
    double? amount,
    String? originalMessage,
    bool? isChecked,
    String? assignedCategory,
    Color? categoryColor,
    IconData? categoryIcon,
  }) {
    return TransactionDraft(
      id: id ?? this.id,
      merchant: merchant ?? this.merchant,
      date: date ?? this.date,
      amount: amount ?? this.amount,
      originalMessage: originalMessage ?? this.originalMessage,
      isChecked: isChecked ?? this.isChecked,
      assignedCategory: assignedCategory ?? this.assignedCategory,
      categoryColor: categoryColor ?? this.categoryColor,
      categoryIcon: categoryIcon ?? this.categoryIcon,
    );
  }
}

class SmsUiState {
  final List<TransactionDraft> drafts;
  final bool isLoading;
  final String? error;

  const SmsUiState({
    this.drafts = const [],
    this.isLoading = false,
    this.error,
  });

  SmsUiState copyWith({
    List<TransactionDraft>? drafts,
    bool? isLoading,
    String? error,
  }) {
    return SmsUiState(
      drafts: drafts ?? this.drafts,
      isLoading: isLoading ?? this.isLoading,
      error: error ?? this.error,
    );
  }
}
