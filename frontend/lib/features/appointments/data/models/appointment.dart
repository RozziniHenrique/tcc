enum AppointmentStatus {
  scheduled('AGENDADO', 'Agendado'),
  completed('CONCLUIDO', 'Concluído'),
  cancelled('CANCELADO', 'Cancelado');

  const AppointmentStatus(this.apiValue, this.label);
  final String apiValue;
  final String label;

  static AppointmentStatus fromApi(String value) {
    return values.firstWhere((status) => status.apiValue == value);
  }
}

class AppointmentFilters {
  const AppointmentFilters({
    this.status,
    this.start,
    this.end,
    this.courseId,
    this.studentId,
    this.clientId,
    this.unitId,
  });

  final AppointmentStatus? status;
  final DateTime? start;
  final DateTime? end;
  final int? courseId;
  final int? studentId;
  final int? clientId;
  final int? unitId;

  AppointmentFilters copyWith({
    AppointmentStatus? status,
    DateTime? start,
    DateTime? end,
    int? courseId,
    int? studentId,
    int? clientId,
    int? unitId,
    bool clearStatus = false,
    bool clearPeriod = false,
    bool clearCourse = false,
    bool clearStudent = false,
    bool clearClient = false,
    bool clearUnit = false,
  }) => AppointmentFilters(
    status: clearStatus ? null : status ?? this.status,
    start: clearPeriod ? null : start ?? this.start,
    end: clearPeriod ? null : end ?? this.end,
    courseId: clearCourse ? null : courseId ?? this.courseId,
    studentId: clearStudent ? null : studentId ?? this.studentId,
    clientId: clearClient ? null : clientId ?? this.clientId,
    unitId: clearUnit ? null : unitId ?? this.unitId,
  );

  @override
  bool operator ==(Object other) =>
      other is AppointmentFilters &&
      other.status == status &&
      other.start == start &&
      other.end == end &&
      other.courseId == courseId &&
      other.studentId == studentId &&
      other.clientId == clientId &&
      other.unitId == unitId;

  @override
  int get hashCode =>
      Object.hash(status, start, end, courseId, studentId, clientId, unitId);
}

class AvailabilityQuery {
  const AvailabilityQuery({required this.courseId, required this.date});

  final int courseId;
  final DateTime date;

  @override
  bool operator ==(Object other) =>
      other is AvailabilityQuery &&
      other.courseId == courseId &&
      other.date == date;

  @override
  int get hashCode => Object.hash(courseId, date);
}

class AvailableSlot {
  const AvailableSlot({
    required this.dateTime,
    required this.availableStudents,
  });

  final DateTime dateTime;
  final int availableStudents;

  factory AvailableSlot.fromJson(Map<String, dynamic> json) => AvailableSlot(
    dateTime: DateTime.parse(json['dataHora'] as String),
    availableStudents: (json['quantidadeAlunosDisponiveis'] as num).toInt(),
  );
}

class Appointment {
  const Appointment({
    required this.id,
    required this.clientName,
    required this.studentName,
    required this.courseName,
    required this.unitName,
    required this.dateTime,
    required this.amount,
    required this.status,
  });

  final int id;
  final String clientName;
  final String studentName;
  final String courseName;
  final String unitName;
  final DateTime dateTime;
  final double amount;
  final AppointmentStatus status;

  factory Appointment.fromJson(Map<String, dynamic> json) => Appointment(
    id: (json['id'] as num).toInt(),
    clientName: json['nomeCliente'] as String? ?? '',
    studentName: json['nomeAluno'] as String? ?? '',
    courseName: json['nomeCurso'] as String? ?? '',
    unitName: json['nomeUnidade'] as String? ?? 'Não informada',
    dateTime: DateTime.parse(json['dataHora'] as String),
    amount: (json['valorNoAto'] as num?)?.toDouble() ?? 0,
    status: AppointmentStatus.fromApi(json['status'] as String),
  );
}

class PendingEvaluation {
  const PendingEvaluation({
    required this.appointmentId,
    required this.studentName,
    required this.courseName,
    required this.dateTime,
  });

  final int appointmentId;
  final String studentName;
  final String courseName;
  final DateTime dateTime;

  factory PendingEvaluation.fromJson(Map<String, dynamic> json) =>
      PendingEvaluation(
        appointmentId: (json['idAgendamento'] as num).toInt(),
        studentName: json['nomeAluno'] as String,
        courseName: json['nomeCurso'] as String,
        dateTime: DateTime.parse(json['dataHora'] as String),
      );
}
