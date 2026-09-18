class DashboardReport {
  const DashboardReport({
    required this.start,
    required this.end,
    required this.totalAppointments,
    required this.revenue,
    required this.studentsByCourse,
    required this.appointmentsByCourse,
  });

  final DateTime start;
  final DateTime end;
  final int totalAppointments;
  final double revenue;
  final List<CourseMetric> studentsByCourse;
  final List<CourseMetric> appointmentsByCourse;

  factory DashboardReport.fromJson(Map<String, dynamic> json) {
    final summary = json['resumo'] as Map<String, dynamic>? ?? const {};
    return DashboardReport(
      start: DateTime.parse(json['inicio'] as String),
      end: DateTime.parse(json['fim'] as String),
      totalAppointments: (summary['totalAgendamentos'] as num?)?.toInt() ?? 0,
      revenue: (summary['faturamentoTotal'] as num?)?.toDouble() ?? 0,
      studentsByCourse: _metrics(
        json['alunosPorCurso'],
        quantityKey: 'quantidadeAlunos',
      ),
      appointmentsByCourse: _metrics(
        json['agendamentosPorCurso'],
        quantityKey: 'quantidadeAgendamentos',
        amountKey: 'faturamentoTotal',
      ),
    );
  }

  static List<CourseMetric> _metrics(
    Object? raw, {
    required String quantityKey,
    String? amountKey,
  }) {
    if (raw is! List) return const [];
    return raw
        .map(
          (item) => CourseMetric.fromJson(
            item as Map<String, dynamic>,
            quantityKey: quantityKey,
            amountKey: amountKey,
          ),
        )
        .toList();
  }
}

class CourseMetric {
  const CourseMetric({
    required this.courseId,
    required this.courseName,
    required this.quantity,
    this.amount,
  });

  final int courseId;
  final String courseName;
  final int quantity;
  final double? amount;

  factory CourseMetric.fromJson(
    Map<String, dynamic> json, {
    required String quantityKey,
    String? amountKey,
  }) => CourseMetric(
    courseId: (json['idCurso'] as num).toInt(),
    courseName: json['nomeCurso'] as String,
    quantity: (json[quantityKey] as num?)?.toInt() ?? 0,
    amount: amountKey == null ? null : (json[amountKey] as num?)?.toDouble(),
  );
}
