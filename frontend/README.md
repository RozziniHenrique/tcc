# STFER — Flutter

Aplicativo móvel e painel web responsivo do STFER. O mesmo projeto Flutter
atende clientes, alunos, funcionários e equipe de gestão, exibindo recursos de
acordo com os perfis retornados pelo endpoint `GET /me`.

## Recursos implementados

- autenticação JWT com access token e refresh token;
- restauração automática da sessão e logout;
- recuperação e redefinição de senha;
- cadastro público de cliente;
- navegação protegida por perfil e função;
- agenda com filtros operacionais por período, status, curso, unidade e pessoa;
- criação de agendamento com consulta dos horários realmente disponíveis;
- avaliações pendentes;
- consulta de cursos, serviços e unidades;
- atualização do próprio perfil;
- gestão de clientes, alunos e funcionários;
- dashboard gerencial por período e exportação em CSV, PDF e Excel;
- acompanhamento do desempenho dos alunos para professores e gestão;
- layout responsivo para Android e Web.

## Arquitetura

O código está organizado por funcionalidade:

```text
lib/
├── core/
│   ├── config/
│   ├── download/
│   ├── models/
│   ├── network/
│   ├── providers/
│   ├── router/
│   ├── storage/
│   ├── theme/
│   ├── utils/
│   └── widgets/
└── features/
    ├── appointments/
    ├── auth/
    ├── catalog/
    ├── dashboard/
    ├── evaluations/
    ├── management/
    ├── performance/
    └── profile/
```

- **Dio**: comunicação com a API e renovação automática do token.
- **Riverpod**: estado da sessão e carregamento dos recursos.
- **GoRouter**: rotas, redirecionamento e autorização por perfil.
- **Flutter Secure Storage**: armazenamento local dos tokens.

## Executar

Com o backend disponível em `http://localhost:8080`:

```bash
flutter pub get
flutter run -d chrome
```

No emulador Android, o endereço padrão do backend é
`http://10.0.2.2:8080`. Em dispositivo físico ou outro ambiente, informe a
URL explicitamente:

```bash
flutter run --dart-define=API_BASE_URL=http://SEU_IP:8080
```

Para compilar o dashboard web:

```bash
flutter build web --dart-define=API_BASE_URL=https://api.seudominio.com
```

## Qualidade

Antes de abrir um pull request:

```bash
dart format lib test
flutter analyze
flutter test
flutter build web
```

O backend precisa permitir a origem usada pelo Flutter Web na configuração de
CORS. Em produção, tanto o frontend quanto a API devem utilizar HTTPS.
