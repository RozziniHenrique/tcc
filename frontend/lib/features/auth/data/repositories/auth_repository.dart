import '../../../../core/network/api_client.dart';
import '../../../../core/storage/token_storage.dart';
import '../models/auth_tokens.dart';
import '../models/authenticated_user.dart';
import '../models/login_request.dart';

class AuthRepository {
  AuthRepository(this._apiClient, this._tokenStorage);

  final ApiClient _apiClient;
  final TokenStorage _tokenStorage;

  Future<AuthTokens> login({
    required String email,
    required String senha,
  }) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      '/auth/login',
      data: LoginRequest(email: email, senha: senha).toJson(),
    );

    final data = response.data;

    if (data == null) {
      throw const FormatException('Resposta de autenticação vazia.');
    }

    final tokens = AuthTokens.fromJson(data);

    await _tokenStorage.saveTokens(
      accessToken: tokens.accessToken,
      refreshToken: tokens.refreshToken,
    );

    return tokens;
  }

  Future<AuthenticatedUser> getCurrentUser() async {
    final response = await _apiClient.dio.get<Map<String, dynamic>>('/me');
    final data = response.data;

    if (data == null) {
      throw const FormatException('Resposta do perfil vazia.');
    }

    return AuthenticatedUser.fromJson(data);
  }

  Future<void> logout() async {
    final refreshToken = await _tokenStorage.readRefreshToken();

    try {
      if (refreshToken != null && refreshToken.isNotEmpty) {
        await _apiClient.dio.post<void>(
          '/auth/logout',
          data: {'refreshToken': refreshToken},
        );
      }
    } finally {
      await _tokenStorage.deleteTokens();
    }
  }

  Future<String> requestPasswordReset(String email) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      '/auth/password/forgot',
      data: {'email': email},
    );
    return response.data?['mensagem'] as String? ??
        'Se o e-mail existir, o código será enviado.';
  }

  Future<bool> verifyPasswordCode(String email, String code) async {
    final response = await _apiClient.dio.post<Map<String, dynamic>>(
      '/auth/password/verify',
      data: {'email': email, 'codigo': code},
    );
    return response.data?['valido'] as bool? ?? false;
  }

  Future<void> resetPassword({
    required String email,
    required String code,
    required String password,
  }) async {
    await _apiClient.dio.post<void>(
      '/auth/password/reset',
      data: {
        'email': email,
        'codigo': code,
        'novaSenha': password,
        'confirmacaoSenha': password,
      },
    );
  }

  Future<void> registerClient({
    required String name,
    required String email,
    required String password,
    required String phone,
    required String cpf,
    required String address,
    String? notes,
  }) async {
    await _apiClient.dio.post<void>(
      '/clientes',
      data: {
        'nome': name,
        'email': email,
        'senha': password,
        'telefone': phone,
        'cpf': cpf,
        'endereco': address,
        'observacoes': notes,
      },
    );
  }
}
