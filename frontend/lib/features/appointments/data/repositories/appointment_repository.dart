import '../../../../core/models/page_result.dart';
import '../../../../core/network/api_client.dart';
import '../models/appointment.dart';

class AppointmentRepository {
  const AppointmentRepository(this._client);
  final ApiClient _client;

  Future<List<Appointment>> list({
    AppointmentFilters filters = const AppointmentFilters(),
  }) async {
    final response = await _client.dio.get<Map<String, dynamic>>(
      '/agendamentos',
      queryParameters: {
        'size': 100,
        'sort': 'dataHora,desc',
        if (filters.status != null) 'status': filters.status!.apiValue,
        if (filters.start != null) 'inicio': _date(filters.start!),
        if (filters.end != null) 'fim': _date(filters.end!),
        if (filters.courseId != null) 'idCurso': filters.courseId,
        if (filters.studentId != null) 'idAluno': filters.studentId,
        if (filters.clientId != null) 'idCliente': filters.clientId,
        if (filters.unitId != null) 'idUnidade': filters.unitId,
      },
    );
    return PageResult<Appointment>.fromJson(
      response.data!,
      Appointment.fromJson,
    ).content;
  }

  Future<List<AvailableSlot>> availability({
    required int courseId,
    required DateTime date,
  }) async {
    final response = await _client.dio.get<List<dynamic>>(
      '/agendamentos/disponibilidade',
      queryParameters: {'idCurso': courseId, 'data': _date(date)},
    );
    return response.data!
        .map((item) => AvailableSlot.fromJson(item as Map<String, dynamic>))
        .toList();
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

  String _date(DateTime value) {
    String two(int number) => number.toString().padLeft(2, '0');
    return '${value.year}-${two(value.month)}-${two(value.day)}';
  }
}
