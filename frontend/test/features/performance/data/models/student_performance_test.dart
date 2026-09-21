import 'package:flutter_test/flutter_test.dart';
import 'package:stfer_app/features/performance/data/models/student_performance.dart';

void main() {
  test('deve interpretar desempenho e calcular taxa de conclusão', () {
    final performance = StudentPerformance.fromJson({
      'idAluno': 10,
      'nomeAluno': 'Ana',
      'idCurso': 2,
      'nomeCurso': 'Estética',
      'totalAgendamentos': 10,
      'totalConcluidos': 8,
      'totalCancelados': 2,
      'mediaAvaliacoes': 4.5,
      'quantidadeAvaliacoes': 6,
    });

    expect(performance.studentName, 'Ana');
    expect(performance.completionRate, 80);
    expect(performance.averageRating, 4.5);
  });

  test('deve retornar taxa zero sem agendamentos', () {
    const performance = StudentPerformance(
      studentId: 10,
      studentName: 'Ana',
      courseId: 2,
      courseName: 'Estética',
      totalAppointments: 0,
      completedAppointments: 0,
      cancelledAppointments: 0,
      averageRating: 0,
      ratingCount: 0,
    );

    expect(performance.completionRate, 0);
  });
}
