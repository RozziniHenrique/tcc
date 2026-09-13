enum UserProfile {
  student('ALUNO'),
  employee('FUNCIONARIO'),
  client('CLIENTE');

  const UserProfile(this.apiValue);

  final String apiValue;

  static UserProfile fromApi(String value) {
    return values.firstWhere(
      (profile) => profile.apiValue == value,
      orElse: () => throw FormatException('Perfil desconhecido: $value'),
    );
  }
}

enum EmployeeRole {
  professor('PROFESSOR'),
  attendant('ATENDENTE'),
  manager('GESTOR'),
  supervisor('SUPERVISOR'),
  admin('ADMIN');

  const EmployeeRole(this.apiValue);

  final String apiValue;

  static EmployeeRole? fromApi(String? value) {
    if (value == null) {
      return null;
    }

    return values.firstWhere(
      (role) => role.apiValue == value,
      orElse: () => throw FormatException('Função desconhecida: $value'),
    );
  }
}

class AuthenticatedUser {
  const AuthenticatedUser({
    required this.id,
    required this.name,
    required this.cpf,
    required this.email,
    required this.phone,
    required this.address,
    required this.profiles,
    required this.employeeRole,
  });

  final int id;
  final String name;
  final String cpf;
  final String email;
  final String phone;
  final String address;
  final Set<UserProfile> profiles;
  final EmployeeRole? employeeRole;

  factory AuthenticatedUser.fromJson(Map<String, dynamic> json) {
    return AuthenticatedUser(
      id: (json['id'] as num).toInt(),
      name: json['nome'] as String,
      cpf: json['cpf'] as String,
      email: json['email'] as String,
      phone: json['telefone'] as String,
      address: json['enderecoCompleto'] as String,
      profiles: (json['perfis'] as List<dynamic>)
          .map((profile) => UserProfile.fromApi(profile as String))
          .toSet(),
      employeeRole: EmployeeRole.fromApi(json['funcao'] as String?),
    );
  }

  bool hasProfile(UserProfile profile) => profiles.contains(profile);
}
