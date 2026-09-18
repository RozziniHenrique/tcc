import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:stfer_app/core/network/api_exception.dart';

void main() {
  test('deve interpretar a resposta de erro padronizada do backend', () {
    final error = DioException(
      requestOptions: RequestOptions(path: '/agendamentos'),
      response: Response<Map<String, dynamic>>(
        requestOptions: RequestOptions(path: '/agendamentos'),
        statusCode: 400,
        data: {
          'error': 'VALIDATION_ERROR',
          'message': 'Dados inválidos.',
          'fields': {'dataHora': 'Horário inválido.'},
        },
      ),
    );

    final exception = ApiException.fromDio(error);

    expect(exception.statusCode, 400);
    expect(exception.code, 'VALIDATION_ERROR');
    expect(exception.message, 'Dados inválidos.');
    expect(exception.fields['dataHora'], 'Horário inválido.');
  });
}
