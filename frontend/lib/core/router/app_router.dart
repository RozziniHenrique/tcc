import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../permissions/access_policy.dart';
import '../platform/app_platform.dart';
import '../../features/auth/data/models/authenticated_user.dart';
import '../../features/auth/presentation/controllers/auth_controller.dart';
import '../../features/auth/presentation/pages/client_registration_page.dart';
import '../../features/auth/presentation/pages/login_page.dart';
import '../../features/auth/presentation/pages/password_recovery_page.dart';
import '../../features/appointments/presentation/pages/appointments_page.dart';
import '../../features/appointments/presentation/pages/new_appointment_page.dart';
import '../../features/catalog/presentation/pages/catalog_page.dart';
import '../../features/dashboard/presentation/pages/dashboard_page.dart';
import '../../features/evaluations/presentation/pages/evaluations_page.dart';
import '../../features/management/presentation/pages/people_page.dart';

import '../../features/profile/presentation/pages/profile_page.dart';
import '../widgets/app_shell.dart';

final appRouterProvider = Provider<GoRouter>((ref) {
  final refreshNotifier = RouterRefreshNotifier();

  ref.listen(authControllerProvider, (_, _) {
    refreshNotifier.refresh();
  });

  final router = GoRouter(
    initialLocation: '/splash',
    refreshListenable: refreshNotifier,
    redirect: (context, state) {
      final authState = ref.read(authControllerProvider);
      final location = state.matchedLocation;

      if (authState.isLoading) {
        return location == '/splash' ? null : '/splash';
      }

      final user = authState.value;
      final publicRoute =
          location == '/login' ||
          location == '/recuperar-senha' ||
          location == '/cadastro';

      if (user == null) {
        return publicRoute ? null : '/login';
      }

      final platform = AppPlatformInfo.current;

      if (!AccessPolicy.canUsePlatform(user, platform)) {
        return location == '/plataforma-indisponivel'
            ? null
            : '/plataforma-indisponivel';
      }

      final initialRoute = _initialRouteFor(user);

      if (publicRoute ||
          location == '/splash' ||
          location == '/plataforma-indisponivel') {
        return initialRoute;
      }

      if (!_canAccess(user, location)) {
        return initialRoute;
      }

      return null;
    },
    routes: [
      GoRoute(
        path: '/splash',
        builder: (context, state) =>
            const Scaffold(body: Center(child: CircularProgressIndicator())),
      ),
      GoRoute(path: '/login', builder: (context, state) => const LoginPage()),
      GoRoute(
        path: '/cadastro',
        builder: (context, state) => const ClientRegistrationPage(),
      ),
      GoRoute(
        path: '/recuperar-senha',
        builder: (context, state) => const PasswordRecoveryPage(),
      ),
      GoRoute(
        path: '/plataforma-indisponivel',
        builder: (context, state) => const _UnsupportedPlatformPage(),
      ),
      ShellRoute(
        builder: (context, state, child) => AppShell(child: child),
        routes: [
          GoRoute(
            path: '/dashboard',
            builder: (context, state) => const DashboardPage(),
          ),
          GoRoute(
            path: '/agendamentos',
            builder: (context, state) => const AppointmentsPage(),
          ),
          GoRoute(
            path: '/novo-agendamento',
            builder: (context, state) => const NewAppointmentPage(),
          ),
          GoRoute(
            path: '/avaliacoes',
            builder: (context, state) => const EvaluationsPage(),
          ),
          GoRoute(
            path: '/catalogo',
            builder: (context, state) => const CatalogPage(),
          ),
          GoRoute(
            path: '/pessoas',
            builder: (context, state) => const PeoplePage(),
          ),
          GoRoute(
            path: '/perfil',
            builder: (context, state) => const ProfilePage(),
          ),
        ],
      ),
    ],
    errorBuilder: (context, state) => Scaffold(
      body: Center(
        child: Column(
          mainAxisSize: MainAxisSize.min,
          children: [
            const Text('Página não encontrada.'),
            const SizedBox(height: 12),
            FilledButton(
              onPressed: () => context.go('/agendamentos'),
              child: const Text('Voltar'),
            ),
          ],
        ),
      ),
    ),
  );

  ref.onDispose(() {
    router.dispose();
    refreshNotifier.dispose();
  });

  return router;
});

class RouterRefreshNotifier extends ChangeNotifier {
  void refresh() {
    notifyListeners();
  }
}

String _initialRouteFor(AuthenticatedUser user) {
  final platform = AppPlatformInfo.current;

  if (AccessPolicy.allows(user, platform, AppCapability.viewDashboard)) {
    return '/dashboard';
  }

  if (AccessPolicy.allows(user, platform, AppCapability.viewAllAppointments) ||
      AccessPolicy.allows(user, platform, AppCapability.viewOwnAppointments)) {
    return '/agendamentos';
  }

  return '/catalogo';
}

bool _canAccess(AuthenticatedUser user, String location) {
  final platform = AppPlatformInfo.current;

  bool allows(AppCapability capability) {
    return AccessPolicy.allows(user, platform, capability);
  }

  if (location.startsWith('/dashboard')) {
    return allows(AppCapability.viewDashboard);
  }

  if (location.startsWith('/pessoas')) {
    return allows(AppCapability.viewClients) ||
        allows(AppCapability.viewStudents) ||
        allows(AppCapability.viewEmployees);
  }

  if (location.startsWith('/novo-agendamento')) {
    return allows(AppCapability.createOwnAppointment);
  }

  if (location.startsWith('/avaliacoes')) {
    return allows(AppCapability.evaluateAppointments);
  }

  if (location.startsWith('/agendamentos')) {
    return allows(AppCapability.viewAllAppointments) ||
        allows(AppCapability.viewOwnAppointments);
  }

  if (location.startsWith('/catalogo')) {
    return allows(AppCapability.viewCatalog);
  }

  if (location.startsWith('/perfil')) {
    return allows(AppCapability.editProfile);
  }

  return true;
}

class _UnsupportedPlatformPage extends ConsumerWidget {
  const _UnsupportedPlatformPage();

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Scaffold(
      body: Center(
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: ConstrainedBox(
            constraints: const BoxConstraints(maxWidth: 480),
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                const Icon(Icons.phone_android, size: 64),
                const SizedBox(height: 24),
                Text(
                  'Acesso disponível pelo aplicativo',
                  textAlign: TextAlign.center,
                  style: Theme.of(context).textTheme.headlineSmall,
                ),
                const SizedBox(height: 12),
                const Text(
                  'Este perfil utiliza o aplicativo mobile do STFER.',
                  textAlign: TextAlign.center,
                ),
                const SizedBox(height: 24),
                FilledButton(
                  onPressed: () {
                    ref.read(authControllerProvider.notifier).logout();
                  },
                  child: const Text('Sair'),
                ),
              ],
            ),
          ),
        ),
      ),
    );
  }
}
