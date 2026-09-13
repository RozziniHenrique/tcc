import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_client.dart';
import '../../../../core/storage/token_storage.dart';
import '../../data/models/authenticated_user.dart';
import '../../data/repositories/auth_repository.dart';

final tokenStorageProvider = Provider<TokenStorage>((ref) {
  return TokenStorage();
});

final apiClientProvider = Provider<ApiClient>((ref) {
  return ApiClient(tokenStorage: ref.watch(tokenStorageProvider));
});

final authRepositoryProvider = Provider<AuthRepository>((ref) {
  return AuthRepository(
    ref.watch(apiClientProvider),
    ref.watch(tokenStorageProvider),
  );
});

final authControllerProvider =
    AsyncNotifierProvider<AuthController, AuthenticatedUser?>(
      AuthController.new,
    );

class AuthController extends AsyncNotifier<AuthenticatedUser?> {
  AuthRepository get _repository => ref.read(authRepositoryProvider);

  TokenStorage get _tokenStorage => ref.read(tokenStorageProvider);

  @override
  Future<AuthenticatedUser?> build() async {
    final accessToken = await _tokenStorage.readAccessToken();

    if (accessToken == null || accessToken.isEmpty) {
      return null;
    }

    try {
      return await _repository.getCurrentUser();
    } catch (_) {
      await _tokenStorage.deleteTokens();
      return null;
    }
  }

  Future<bool> login({required String email, required String senha}) async {
    state = const AsyncLoading();

    state = await AsyncValue.guard<AuthenticatedUser?>(() async {
      await _repository.login(email: email, senha: senha);
      return _repository.getCurrentUser();
    });

    return !state.hasError;
  }
}
