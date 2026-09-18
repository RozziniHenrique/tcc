class PersonSummary {
  const PersonSummary({
    required this.id,
    required this.name,
    required this.email,
    this.phone,
    this.detail,
    this.active,
  });

  final int id;
  final String name;
  final String email;
  final String? phone;
  final String? detail;
  final bool? active;

  factory PersonSummary.client(Map<String, dynamic> json) => PersonSummary(
    id: (json['id'] as num).toInt(),
    name: json['nome'] as String,
    email: json['email'] as String,
    phone: json['telefone'] as String?,
    active: json['ativo'] as bool?,
  );

  factory PersonSummary.student(Map<String, dynamic> json) => PersonSummary(
    id: (json['id'] as num).toInt(),
    name: json['nome'] as String,
    email: json['email'] as String,
    detail: json['nomeCurso'] as String?,
  );

  factory PersonSummary.employee(Map<String, dynamic> json) => PersonSummary(
    id: (json['id'] as num).toInt(),
    name: json['nome'] as String,
    email: json['email'] as String,
    detail: json['funcao'] as String?,
  );
}
