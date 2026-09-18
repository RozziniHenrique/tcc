import 'package:dio/dio.dart';

class ApiException implements Exception {
  const ApiException({
    required this.message,
    this.statusCode,
    this.code,
    this.fields = const {},
  });

  final String message;
  final int? statusCode;
  final String? code;
  final Map<String, String> fields;

  factory ApiException.fromDio(DioException exception) {
    final data = exception.response?.data;
    if (data is Map<String, dynamic>) {
      final rawFields = data['fields'];
      return ApiException(
        message: data['message'] as String? ?? _networkMessage(exception),
        statusCode: exception.response?.statusCode,
        code: data['error'] as String?,
        fields: rawFields is Map
            ? rawFields.map(
                (key, value) => MapEntry(key.toString(), value.toString()),
              )
            : const {},
      );
    }
    return ApiException(
      message: _networkMessage(exception),
      statusCode: exception.response?.statusCode,
    );
  }

  static String messageFor(Object error) {
    if (error is ApiException) return error.message;
    if (error is DioException) return ApiException.fromDio(error).message;
    return 'Não foi possível concluir a operação.';
  }

  static String _networkMessage(DioException exception) {
    return switch (exception.type) {
      DioExceptionType.connectionTimeout ||
      DioExceptionType.sendTimeout ||
      DioExceptionType.receiveTimeout =>
        'A conexão demorou mais que o esperado.',
      DioExceptionType.connectionError =>
        'Não foi possível conectar ao servidor.',
      _ => 'Não foi possível concluir a operação.',
    };
  }

  @override
  String toString() => message;
}
