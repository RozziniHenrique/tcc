import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../controllers/auth_controller.dart';

class ClientRegistrationPage extends ConsumerStatefulWidget {
  const ClientRegistrationPage({super.key});

  @override
  ConsumerState<ClientRegistrationPage> createState() =>
      _ClientRegistrationPageState();
}

class _ClientRegistrationPageState
    extends ConsumerState<ClientRegistrationPage> {
  final _formKey = GlobalKey<FormState>();
  final _name = TextEditingController();
  final _email = TextEditingController();
  final _password = TextEditingController();
  final _passwordConfirmation = TextEditingController();
  final _phone = TextEditingController();
  final _cpf = TextEditingController();
  final _address = TextEditingController();
  final _notes = TextEditingController();
  var _loading = false;
  var _obscurePassword = true;

  @override
  void dispose() {
    _name.dispose();
    _email.dispose();
    _password.dispose();
    _passwordConfirmation.dispose();
    _phone.dispose();
    _cpf.dispose();
    _address.dispose();
    _notes.dispose();
    super.dispose();
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      body: SafeArea(
        child: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(24),
            child: ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 680),
              child: Card(
                child: Padding(
                  padding: const EdgeInsets.all(28),
                  child: Form(
                    key: _formKey,
                    child: Column(
                      crossAxisAlignment: CrossAxisAlignment.stretch,
                      children: [
                        Text(
                          'Criar conta de cliente',
                          style: Theme.of(context).textTheme.headlineSmall,
                        ),
                        const SizedBox(height: 8),
                        const Text(
                          'Preencha seus dados para acessar os agendamentos.',
                        ),
                        const SizedBox(height: 24),
                        _field(
                          controller: _name,
                          label: 'Nome completo',
                          validator: (value) => _required(value, min: 2),
                        ),
                        const SizedBox(height: 16),
                        _field(
                          controller: _email,
                          label: 'E-mail',
                          keyboardType: TextInputType.emailAddress,
                          validator: (value) {
                            final email = value?.trim() ?? '';
                            return email.contains('@')
                                ? null
                                : 'Informe um e-mail válido.';
                          },
                        ),
                        const SizedBox(height: 16),
                        _field(
                          controller: _cpf,
                          label: 'CPF (somente números)',
                          keyboardType: TextInputType.number,
                          maxLength: 11,
                          validator: (value) => _digits(value, 11, 11, 'CPF'),
                        ),
                        const SizedBox(height: 16),
                        _field(
                          controller: _phone,
                          label: 'Telefone (somente números)',
                          keyboardType: TextInputType.phone,
                          maxLength: 11,
                          validator: (value) =>
                              _digits(value, 10, 11, 'telefone'),
                        ),
                        const SizedBox(height: 16),
                        _field(
                          controller: _address,
                          label: 'Endereço completo',
                          validator: (value) => _required(value, min: 2),
                        ),
                        const SizedBox(height: 16),
                        _field(
                          controller: _password,
                          label: 'Senha',
                          obscureText: _obscurePassword,
                          suffixIcon: IconButton(
                            onPressed: () => setState(
                              () => _obscurePassword = !_obscurePassword,
                            ),
                            icon: Icon(
                              _obscurePassword
                                  ? Icons.visibility_outlined
                                  : Icons.visibility_off_outlined,
                            ),
                          ),
                          validator: _validatePassword,
                        ),
                        const SizedBox(height: 16),
                        _field(
                          controller: _passwordConfirmation,
                          label: 'Confirme a senha',
                          obscureText: _obscurePassword,
                          validator: (value) => value == _password.text
                              ? null
                              : 'A confirmação da senha não confere.',
                        ),
                        const SizedBox(height: 16),
                        _field(
                          controller: _notes,
                          label: 'Observações (opcional)',
                          maxLines: 3,
                          maxLength: 1000,
                        ),
                        const SizedBox(height: 24),
                        FilledButton(
                          onPressed: _loading ? null : _submit,
                          child: _loading
                              ? const SizedBox.square(
                                  dimension: 20,
                                  child: CircularProgressIndicator(
                                    strokeWidth: 2,
                                  ),
                                )
                              : const Text('Criar conta'),
                        ),
                        TextButton(
                          onPressed: _loading
                              ? null
                              : () => context.go('/login'),
                          child: const Text('Já tenho uma conta'),
                        ),
                      ],
                    ),
                  ),
                ),
              ),
            ),
          ),
        ),
      ),
    );
  }

  Widget _field({
    required TextEditingController controller,
    required String label,
    TextInputType? keyboardType,
    String? Function(String?)? validator,
    Widget? suffixIcon,
    bool obscureText = false,
    int maxLines = 1,
    int? maxLength,
  }) {
    return TextFormField(
      controller: controller,
      keyboardType: keyboardType,
      validator: validator,
      obscureText: obscureText,
      maxLines: maxLines,
      maxLength: maxLength,
      decoration: InputDecoration(labelText: label, suffixIcon: suffixIcon),
    );
  }

  String? _required(String? value, {required int min}) {
    return (value?.trim().length ?? 0) >= min
        ? null
        : 'Este campo é obrigatório.';
  }

  String? _digits(String? value, int min, int max, String field) {
    final digits = value?.replaceAll(RegExp(r'\D'), '') ?? '';
    return digits.length >= min && digits.length <= max
        ? null
        : 'Informe um $field válido.';
  }

  String? _validatePassword(String? value) {
    final length = value?.length ?? 0;
    return length >= 8 && length <= 72
        ? null
        : 'A senha deve conter entre 8 e 72 caracteres.';
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate()) return;
    setState(() => _loading = true);
    try {
      await ref
          .read(authRepositoryProvider)
          .registerClient(
            name: _name.text.trim(),
            email: _email.text.trim(),
            password: _password.text,
            phone: _phone.text.replaceAll(RegExp(r'\D'), ''),
            cpf: _cpf.text.replaceAll(RegExp(r'\D'), ''),
            address: _address.text.trim(),
            notes: _notes.text.trim().isEmpty ? null : _notes.text.trim(),
          );
      if (mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Conta criada. Agora faça o login.')),
        );
        context.go('/login');
      }
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(ApiException.messageFor(error))));
      }
    } finally {
      if (mounted) setState(() => _loading = false);
    }
  }
}
