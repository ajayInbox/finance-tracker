import 'package:flutter/foundation.dart';
import 'dart:io' show Platform;

class ApiConstants {
  static String get baseUrl {
    if (kIsWeb) {
      return "http://localhost:8080/";
    }
    try {
      if (Platform.isAndroid) {
        return "http://10.0.2.2:8080/";
      }
    } catch (_) {}
    return "http://localhost:8080/";
  }
  static String register = "/auth/register";
  static String login = "/auth/login";
  static String refresh = "/auth/refresh";
  static String logout = "/auth/logout";
  static String userProfile = "/auth/user";
  static String getTransactions = "/api/v1/transactions";
  static String createTransaction = "/api/v1/transactions";
  static String updateTransaction = "/api/v1/transactions";
  static String updateBatchTransactions = "/api/v1/transactions/batch";
  static String deleteTransaction = "/api/v1/transactions";
  static String getAccounts = "/api/v1/accounts";
  static String createAccount = "/api/v1/account";
  static String initializeDefaults = "/api/v1/accounts/initialize-defaults";
  static String getCategories = "/api/v1/categories";
  static String createCategory = "/api/v1/categories";
  static String getAllChildrenCategories = "/api/v1/categories/subcategories";
  static String avgDaily = "/api/v1/transactions/avg-daily";
  static String expenseReport = "/api/v1/transactions/analysis";
  static String exportMessages = "/api/v1/transactions/export-messages";
  static String networthSummary = "/api/v1/networth";
  static String deleteAccount = "/api/v1/accounts";
  static String updateAccount = "/api/v1/accounts";

  // Sync endpoints (used natively via Kotlin, listed here for reference)
  static String syncLatestTimestamp = "/api/sync/latest-timestamp";
  static String syncBatchUpload = "/api/sync/batch-upload";

  // Draft management
  static String deleteDraftsBatch = "/api/v1/transactions/drafts/batch-delete";
}
