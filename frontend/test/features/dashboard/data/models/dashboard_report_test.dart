import 'package:flutter_test/flutter_test.dart';
import 'package:stfer_app/features/dashboard/data/models/dashboard_report.dart';

void main() {
  test('deve interpretar relatório completo', () {
    final report = DashboardReport.fromJson({
      'inicio': '2026-09-01',
      'fim': '2026-09-30',
      'resumo': {'totalAgendamentos': 3, 'faturamentoTotal': 450.0},
      'alunosPorCurso': [
        {'idCurso': 1, 'nomeCurso': 'Estética', 'quantidadeAlunos': 5},
      ],
      'agendamentosPorCurso': [
        {
          'idCurso': 1,
          'nomeCurso': 'Estética',
          'quantidadeAgendamentos': 3,
          'faturamentoTotal': 450.0,
        },
      ],
    });

    expect(report.totalAppointments, 3);
    expect(report.revenue, 450);
    expect(report.studentsByCourse.single.quantity, 5);
  });
}
