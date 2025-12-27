class ApiConstants {

  static String baseUrl = "http://10.0.2.2:8080/";
  //static String baseUrl = "http://localhost:8080/";
  //static String baseUrl = "https://finance-tracker-backend-m1rr.onrender.com";
  static String getTransactions = "/api/v1/transactions";
  static String createTransaction = "/api/v1/transactions";
  static String updateTransaction = "/api/v1/transactions";
  static String deleteTransaction = "/api/v1/transactions";
  static String getAccounts = "/api/v1/accounts";
  static String createAccount = "/api/v1/account";
  static String getCategories = "/api/v1/categories";
  static String avgDaily = "/api/v1/transactions/avg-daily";
  static String expenseReport = "/api/v1/transactions/analysis";
  static String exportMessages = "/api/v1/transactions/export-messages";
  static String networthSummary = "/api/v1/networth";

}
