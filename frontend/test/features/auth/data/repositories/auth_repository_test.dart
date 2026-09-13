import 'package:dio/dio.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';

import 'package:stfer_app/core/network/api_client.dart';
import 'package:stfer_app/core/storage/token_storage.dart';
import 'package:stfer_app/features/auth/data/repositories/auth_repository.dart';
import 'package:stfer_app/features/auth/data/models/authenticated_user.dart';

class MockDio extends Mock implements Dio {}

class MockTokenStorage extends Mock implements TokenStorage {}

void main() {
  late MockDio dio;
  late MockTokenStorage tokenStorage;
  late AuthRepository repository;

  setUp(() {
    dio = MockDio();
    tokenStorage = MockTokenStorage();
    repository = AuthRepository(ApiClient(dio: dio), tokenStorage);
  });

  test('deve autenticar e armazenar os tokens', () async {
    when(
      () => dio.post<Map<String, dynamic>>(
        '/auth/login',
        data: any(named: 'data'),
      ),
    ).thenAnswer(
      (_) async => Response(
        data: {
          'accessToken': 'access-token',
          'refreshToken': 'refresh-token',
          'tokenType': 'Bearer',
          'expiresIn': 900,
        },
        statusCode: 200,
        requestOptions: RequestOptions(path: '/auth/login'),
      ),
    );

    when(
      () => tokenStorage.saveTokens(
        accessToken: any(named: 'accessToken'),
        refreshToken: any(named: 'refreshToken'),
      ),
    ).thenAnswer((_) async {});

    final resultado = await repository.login(
      email: 'cliente@stfer.com',
      senha: 'senhaSegura123',
    );

    expect(resultado.accessToken, 'access-token');
    expect(resultado.refreshToken, 'refresh-token');
    expect(resultado.tokenType, 'Bearer');
    expect(resultado.expiresIn, 900);

    verify(
      () => dio.post<Map<String, dynamic>>(
        '/auth/login',
        data: any(
          named: 'data',
          that: equals({
            'email': 'cliente@stfer.com',
            'senha': 'senhaSegura123',
          }),
        ),
      ),
    ).called(1);

    verify(
      () => tokenStorage.saveTokens(
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
      ),
    ).called(1);
  });
  test('deve buscar o usuário autenticado', () async {
    when(() => dio.get<Map<String, dynamic>>('/me')).thenAnswer(
      (_) async => Response(
        data: {
          'id': 1,
          'nome': 'Henrique',
          'cpf': '12345678901',
          'email': 'henrique@stfer.com',
          'telefone': '11999999999',
          'enderecoCompleto': 'São Paulo',
          'perfis': ['CLIENTE', 'FUNCIONARIO'],
          'funcao': 'GESTOR',
        },
        statusCode: 200,
        requestOptions: RequestOptions(path: '/me'),
      ),
    );

    final resultado = await repository.getCurrentUser();

    expect(resultado.id, 1);
    expect(resultado.name, 'Henrique');
    expect(resultado.profiles, contains(UserProfile.client));
    expect(resultado.profiles, contains(UserProfile.employee));
    expect(resultado.employeeRole, EmployeeRole.manager);

    verify(() => dio.get<Map<String, dynamic>>('/me')).called(1);
  });
}
