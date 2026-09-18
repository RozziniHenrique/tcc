import 'package:flutter_test/flutter_test.dart';
import 'package:stfer_app/core/models/page_result.dart';

void main() {
  test('deve interpretar paginação estável do backend', () {
    final result = PageResult.fromJson({
      'content': [
        {'id': 1, 'nome': 'Curso'},
      ],
      'page': {'number': 0, 'size': 20, 'totalElements': 1, 'totalPages': 1},
    }, (json) => json['nome'] as String);

    expect(result.content, ['Curso']);
    expect(result.number, 0);
    expect(result.totalElements, 1);
  });
}
