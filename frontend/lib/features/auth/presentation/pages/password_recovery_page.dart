import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../controllers/auth_controller.dart';

class PasswordRecoveryPage extends ConsumerStatefulWidget {
  const PasswordRecoveryPage({super.key});

  @override
  ConsumerState<PasswordRecoveryPage> createState() =>
      _PasswordRecoveryPageState();
}

class _PasswordRecoveryPageState extends ConsumerState<PasswordRecoveryPage> {
  final _email = TextEditingController();
  final _code = TextEditingController();
  final _password = TextEditingController();
  final _passwordConfirmation = TextEditingController();
  var _step = 0;
  var _loading = false;

  @override
  void dispose() {
    _email.dispose();
    _code.dispose();
    _password.dispose();
    _passwordConfirmation.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: Center(
        child: SingleChildScrollView(
          padding: const EdgeInsets.all(24),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 440),
            child: Card(
              child: Padding(
                padding: const EdgeInsets.all(28),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    Text(
                      'Recuperar senha',
                      style: Theme.of(context).textTheme.headlineSmall,
                    ),
                    const SizedBox(height: 8),
                    Text(_description),
                    const SizedBox(height: 24),
                    if (_step == 0)
                      TextField(
                        controller: _email,
                        keyboardType: TextInputType.emailAddress,
                        decoration: const InputDecoration(labelText: 'E-mail'),
                      )
                    else
                      TextField(
                        controller: _code,
                        keyboardType: TextInputType.number,
                        maxLength: 6,
                        decoration: const InputDecoration(
                          labelText: 'Código de 6 dígitos',
                        ),
                      ),
                    if (_step == 2) ...[
                      const SizedBox(height: 16),
                      TextField(
                        controller: _password,
                        obscureText: true,
                        decoration: const InputDecoration(
                          labelText: 'Nova senha',
                        ),
                      ),
                      const SizedBox(height: 16),
                      TextField(
                        controller: _passwordConfirmation,
                        obscureText: true,
                        decoration: const InputDecoration(
                          labelText: 'Confirme a nova senha',
                        ),
                      ),
                    ],
                    const SizedBox(height: 24),
                    FilledButton(
                      onPressed: _loading ? null : _continue,
                      child: _loading
                          ? const SizedBox.square(
                              dimension: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : Text(_buttonLabel),
                    ),
                    TextButton(
                      onPressed: () => context.go('/login'),
                      child: const Text('Voltar ao login'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  String get _description => switch (_step) {
    0 => 'Informe o e-mail da conta para solicitar o código.',
    1 => 'Informe o código recebido para continuar.',
    _ => 'Crie uma senha com 8 a 72 caracteres.',
  };

  String get _buttonLabel => switch (_step) {
    0 => 'Solicitar código',
    1 => 'Verificar código',
    _ => 'Alterar senha',
  };

  Future<void> _continue() async {
    final email = _email.text.trim();
    if (!email.contains('@')) {
      _message('Informe um e-mail válido.');
      return;
    }
    setState(() => _loading = true);
    try {
      final repository = ref.read(authRepositoryProvider);
      if (_step == 0) {
        final message = await repository.requestPasswordReset(email);
        if (mounted) {
          _message(message);
          setState(() => _step = 1);
        }
      } else if (_step == 1) {
        final valid = await repository.verifyPasswordCode(
          email,
          _code.text.trim(),
        );
        if (!valid) throw const ApiException(message: 'Código inválido.');
        if (mounted) setState(() => _step = 2);
      } else {
        if (_password.text.length < 8 || _password.text.length > 72) {
          throw const ApiException(
            message: 'A senha deve conter entre 8 e 72 caracteres.',
          );
        }
        if (_password.text != _passwordConfirmation.text) {
          throw const ApiException(
            message: 'A confirmação da senha não confere.',
          );
        }
        await repository.resetPassword(
          email: email,
          code: _code.text.trim(),
          password: _password.text,
        );
        if (mounted) {
          _message('Senha alterada com sucesso.');
          context.go('/login');
        }
      }
    } catch (error) {
      if (mounted) _message(ApiException.messageFor(error));
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }

  void _message(String value) {
    ScaffoldMessenger.of(context).showSnackBar(SnackBar(content: Text(value)));
  }
}
