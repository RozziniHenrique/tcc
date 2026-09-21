import 'package:flutter_test/flutter_test.dart';
import 'package:stfer_app/core/permissions/access_policy.dart';
import 'package:stfer_app/core/platform/app_platform.dart';
import 'package:stfer_app/features/auth/data/models/authenticated_user.dart';

void main() {
  test('professor pode consultar desempenho, mas não exportar relatórios', () {
    final user = _employee(EmployeeRole.professor);

    expect(
      AccessPolicy.allows(
        user,
        AppPlatform.web,
        AppCapability.viewStudentPerformance,
      ),
      isTrue,
    );
    expect(
      AccessPolicy.allows(user, AppPlatform.web, AppCapability.exportReports),
      isFalse,
    );
  });

  test('gestor pode consultar desempenho e exportar relatórios', () {
    final user = _employee(EmployeeRole.manager);

    expect(
      AccessPolicy.allows(
        user,
        AppPlatform.web,
        AppCapability.viewStudentPerformance,
      ),
      isTrue,
    );
    expect(
      AccessPolicy.allows(user, AppPlatform.web, AppCapability.exportReports),
      isTrue,
    );
  });
}

AuthenticatedUser _employee(EmployeeRole role) => AuthenticatedUser(
  id: 1,
  name: 'Usuário',
  cpf: '12345678901',
  email: 'usuario@teste.com',
  phone: '11999999999',
  address: 'Rua Teste',
  profiles: const {UserProfile.employee},
  employeeRole: role,
);
