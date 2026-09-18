import '../../../../core/network/api_client.dart';
import '../../../auth/data/models/authenticated_user.dart';

class ProfileRepository {
  const ProfileRepository(this._client);
  final ApiClient _client;

  Future<AuthenticatedUser> update({
    required String name,
    required String phone,
    required String address,
  }) async {
    final response = await _client.dio.put<Map<String, dynamic>>(
      '/me',
      data: {'nome': name, 'telefone': phone, 'enderecoCompleto': address},
    );
    return AuthenticatedUser.fromJson(response.data!);
  }
}
