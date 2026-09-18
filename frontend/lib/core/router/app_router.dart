import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

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

      if (user == null) {
        final publicRoute =
            location == '/login' ||
            location == '/recuperar-senha' ||
            location == '/cadastro';
        return publicRoute ? null : '/login';
      }

      final initialRoute = _initialRouteFor(user);

      if (location == '/login' || location == '/splash') {
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
  if (_isManagement(user)) {
    return '/dashboard';
  }
  return '/agendamentos';
}

bool _canAccess(AuthenticatedUser user, String location) {
  if (location.startsWith('/dashboard') || location.startsWith('/pessoas')) {
    return _isManagement(user);
  }

  if (location.startsWith('/novo-agendamento') ||
      location.startsWith('/avaliacoes')) {
    return user.hasProfile(UserProfile.client);
  }

  return true;
}

bool _isManagement(AuthenticatedUser user) {
  return switch (user.employeeRole) {
    EmployeeRole.manager ||
    EmployeeRole.supervisor ||
    EmployeeRole.admin => true,
    _ => false,
  };
}
