import '../../../../core/network/api_client.dart';
import '../models/student_performance.dart';

class PerformanceRepository {
  const PerformanceRepository(this._client);
  final ApiClient _client;

  Future<List<StudentPerformance>> list(PerformanceQuery query) async {
    final response = await _client.dio.get<List<dynamic>>(
      '/relatorios/desempenho-alunos',
      queryParameters: {
        'inicio': _date(query.start),
        'fim': _date(query.end),
        if (query.courseId != null) 'idCurso': query.courseId,
        if (query.studentId != null) 'idAluno': query.studentId,
      },
    );
    return response.data!
        .map(
          (item) => StudentPerformance.fromJson(item as Map<String, dynamic>),
        )
        .toList();
  }

  String _date(DateTime value) {
    String two(int number) => number.toString().padLeft(2, '0');
    return '${value.year}-${two(value.month)}-${two(value.day)}';
  }
}
