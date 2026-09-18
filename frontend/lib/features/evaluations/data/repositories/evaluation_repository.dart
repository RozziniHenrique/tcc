import '../../../../core/network/api_client.dart';
import '../../../appointments/data/models/appointment.dart';

class EvaluationRepository {
  const EvaluationRepository(this._client);
  final ApiClient _client;

  Future<List<PendingEvaluation>> pending() async {
    final response = await _client.dio.get<List<dynamic>>(
      '/avaliacoes/pendentes',
    );
    return (response.data ?? const [])
        .map((item) => PendingEvaluation.fromJson(item as Map<String, dynamic>))
        .toList();
  }

  Future<void> submit({
    required int appointmentId,
    required int rating,
    String? comment,
  }) async {
    await _client.dio.post<void>(
      '/avaliacoes',
      data: {
        'idAgendamento': appointmentId,
        'nota': rating,
        'comentario': comment,
      },
    );
  }
}
