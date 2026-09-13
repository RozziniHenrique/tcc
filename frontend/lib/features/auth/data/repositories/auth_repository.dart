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
}
