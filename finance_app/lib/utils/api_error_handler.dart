import 'package:dio/dio.dart';

class ApiErrorHandler {
  static String getErrorMessage(Object error) {
    if (error is DioException) {
      if (error.response != null && error.response?.data != null) {
        final data = error.response!.data;
        if (data is Map<String, dynamic>) {
          if (data.containsKey('message')) {
            return data['message'];
          } else if (data.containsKey('error')) {
            return data['error'];
          } else if (data.containsKey('detail')) {
            return data['detail'];
          }
        } else if (data is String) {
          return data;
        }
      }
      switch (error.type) {
        case DioExceptionType.connectionTimeout:
          return "Connection timeout";
        case DioExceptionType.sendTimeout:
          return "Send timeout";
        case DioExceptionType.receiveTimeout:
          return "Receive timeout";
        case DioExceptionType.badCertificate:
          return "Bad certificate";
        case DioExceptionType.badResponse:
          return "Bad response";
        case DioExceptionType.cancel:
          return "Request cancelled";
        case DioExceptionType.connectionError:
          return "Connection error";
        case DioExceptionType.unknown:
          return "Unknown error";
      }
    }
    return error.toString();
  }
}
