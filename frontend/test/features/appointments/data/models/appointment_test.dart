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

  test('deve interpretar horário disponível retornado pela API', () {
    final slot = AvailableSlot.fromJson({
      'dataHora': '2026-09-21T09:30:00',
      'quantidadeAlunosDisponiveis': 3,
    });

    expect(slot.dateTime, DateTime(2026, 9, 21, 9, 30));
    expect(slot.availableStudents, 3);
  });

  test('filtros iguais devem possuir igualdade por valor', () {
    final first = AppointmentFilters(
      status: AppointmentStatus.completed,
      start: DateTime(2026, 9, 1),
      end: DateTime(2026, 9, 30),
      courseId: 1,
    );
    final second = AppointmentFilters(
      status: AppointmentStatus.completed,
      start: DateTime(2026, 9, 1),
      end: DateTime(2026, 9, 30),
      courseId: 1,
    );

    expect(first, second);
    expect(first.hashCode, second.hashCode);
  });
}
