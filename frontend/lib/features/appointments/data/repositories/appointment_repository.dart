import '../../../../core/models/page_result.dart';
import '../../../../core/network/api_client.dart';
import '../models/appointment.dart';

class AppointmentRepository {
  const AppointmentRepository(this._client);
  final ApiClient _client;

  Future<List<Appointment>> list({AppointmentStatus? status}) async {
    final response = await _client.dio.get<Map<String, dynamic>>(
      '/agendamentos',
      queryParameters: {
        'size': 100,
        'sort': 'dataHora,desc',
        if (status != null) 'status': status.apiValue,
      },
    );
    return PageResult<Appointment>.fromJson(
      response.data!,
      Appointment.fromJson,
    ).content;
  }

  Future<void> create({
    required int clientId,
    required int courseId,
    required int unitId,
    required List<int> serviceIds,
    required DateTime dateTime,
  }) async {
    await _client.dio.post<void>(
      '/agendamentos',
      data: {
        'idCliente': clientId,
        'idAluno': null,
        'idCurso': courseId,
        'idUnidade': unitId,
        'idServicos': serviceIds,
        'dataHora': dateTime.toIso8601String(),
      },
    );
  }

  Future<void> cancel(int id, String reason) async {
    await _client.dio.delete<void>(
      '/agendamentos/$id',
      data: {'justificativa': reason},
    );
  }

  Future<void> complete(int id) async {
    await _client.dio.patch<void>('/agendamentos/$id/concluir');
  }
}
