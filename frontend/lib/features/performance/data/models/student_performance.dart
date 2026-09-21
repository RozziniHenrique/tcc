class StudentPerformance {
  const StudentPerformance({
    required this.studentId,
    required this.studentName,
    required this.courseId,
    required this.courseName,
    required this.totalAppointments,
    required this.completedAppointments,
    required this.cancelledAppointments,
    required this.averageRating,
    required this.ratingCount,
  });

  final int studentId;
  final String studentName;
  final int courseId;
  final String courseName;
  final int totalAppointments;
  final int completedAppointments;
  final int cancelledAppointments;
  final double averageRating;
  final int ratingCount;

  double get completionRate => totalAppointments == 0
      ? 0
      : completedAppointments / totalAppointments * 100;

  factory StudentPerformance.fromJson(Map<String, dynamic> json) =>
      StudentPerformance(
        studentId: (json['idAluno'] as num).toInt(),
        studentName: json['nomeAluno'] as String,
        courseId: (json['idCurso'] as num).toInt(),
        courseName: json['nomeCurso'] as String,
        totalAppointments: (json['totalAgendamentos'] as num).toInt(),
        completedAppointments: (json['totalConcluidos'] as num).toInt(),
        cancelledAppointments: (json['totalCancelados'] as num).toInt(),
        averageRating: (json['mediaAvaliacoes'] as num?)?.toDouble() ?? 0,
        ratingCount: (json['quantidadeAvaliacoes'] as num).toInt(),
      );
}

class PerformanceQuery {
  const PerformanceQuery({
    required this.start,
    required this.end,
    this.courseId,
    this.studentId,
  });

  final DateTime start;
  final DateTime end;
  final int? courseId;
  final int? studentId;

  @override
  bool operator ==(Object other) =>
      other is PerformanceQuery &&
      other.start == start &&
      other.end == end &&
      other.courseId == courseId &&
      other.studentId == studentId;

  @override
  int get hashCode => Object.hash(start, end, courseId, studentId);
}
