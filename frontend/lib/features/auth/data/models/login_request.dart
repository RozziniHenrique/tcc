class LoginRequest {
  const LoginRequest({required this.email, required this.senha});

  final String email;
  final String senha;

  Map<String, dynamic> toJson() {
    return {'email': email, 'senha': senha};
  }
}
