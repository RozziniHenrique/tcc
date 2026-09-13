import 'package:flutter_test/flutter_test.dart';
import 'package:mocktail/mocktail.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'package:stfer_app/core/storage/token_storage.dart';
import 'package:stfer_app/features/auth/presentation/controllers/auth_controller.dart';
import 'package:stfer_app/main.dart';

class MockTokenStorage extends Mock implements TokenStorage {}

void main() {
  testWidgets('deve exibir a tela de login', (tester) async {
    final tokenStorage = MockTokenStorage();

    when(tokenStorage.readAccessToken).thenAnswer((_) async => null);

    await tester.pumpWidget(
      ProviderScope(
        overrides: [tokenStorageProvider.overrideWithValue(tokenStorage)],
        child: const StferApp(),
      ),
    );

    await tester.pumpAndSettle();

    expect(find.text('Bem-vindo ao STFER'), findsOneWidget);
    expect(find.text('E-mail'), findsOneWidget);
    expect(find.text('Senha'), findsOneWidget);
    expect(find.text('Entrar'), findsOneWidget);
  });
}
