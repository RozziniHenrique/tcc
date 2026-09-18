import 'package:flutter_test/flutter_test.dart';
import 'package:stfer_app/features/appointments/data/models/appointment.dart';

void main() {
  test('deve interpretar agendamento retornado pela API', () {
    final appointment = Appointment.fromJson({
      'id': 1,
      'nomeCliente': 'Cliente',
      'nomeAluno': 'Aluno',
      'nomeCurso': 'Estética',
      'nomeUnidade': 'Centro',
      'dataHora': '2026-09-20T14:00:00',
      'valorNoAto': 150.0,
      'status': 'AGENDADO',
    });

    expect(appointment.id, 1);
    expect(appointment.status, AppointmentStatus.scheduled);
    expect(appointment.amount, 150);
  });
}
