import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../permissions/access_policy.dart';
import '../platform/app_platform.dart';
import '../../features/auth/data/models/authenticated_user.dart';
import '../../features/auth/presentation/controllers/auth_controller.dart';

class AppShell extends ConsumerWidget {
  const AppShell({required this.child, super.key});
  final Widget child;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final user = ref.watch(authControllerProvider).value;
    if (user == null) return child;

    final location = GoRouterState.of(context).uri.path;
    final destinations = _destinations(user);
    final selected = destinations.indexWhere(
      (item) => location.startsWith(item.path),
    );
    final isDesktop = MediaQuery.sizeOf(context).width >= 900;

    void navigate(_Destination destination) => context.go(destination.path);

    if (isDesktop) {
      return Scaffold(
        appBar: AppBar(
          title: const Text('STFER'),
          actions: [_UserMenu(user: user)],
        ),
        body: Row(
          children: [
            NavigationRail(
              extended: MediaQuery.sizeOf(context).width >= 1180,
              selectedIndex: selected < 0 ? 0 : selected,
              onDestinationSelected: (index) => navigate(destinations[index]),
              labelType: MediaQuery.sizeOf(context).width >= 1180
                  ? NavigationRailLabelType.none
                  : NavigationRailLabelType.selected,
              destinations: [
                for (final item in destinations)
                  NavigationRailDestination(
                    icon: Icon(item.icon),
                    selectedIcon: Icon(item.selectedIcon),
                    label: Text(item.label),
                  ),
              ],
            ),
            const VerticalDivider(width: 1),
            Expanded(child: child),
          ],
        ),
      );
    }

    return Scaffold(
      appBar: AppBar(
        title: const Text('STFER'),
        actions: [_UserMenu(user: user)],
      ),
      drawer: NavigationDrawer(
        selectedIndex: selected < 0 ? null : selected,
        onDestinationSelected: (index) {
          Navigator.pop(context);
          navigate(destinations[index]);
        },
        children: [
          Padding(
            padding: const EdgeInsets.fromLTRB(28, 24, 16, 12),
            child: Text(
              user.name,
              style: Theme.of(context).textTheme.titleMedium,
            ),
          ),
          for (final item in destinations)
            NavigationDrawerDestination(
              icon: Icon(item.icon),
              selectedIcon: Icon(item.selectedIcon),
              label: Text(item.label),
            ),
        ],
      ),
      body: child,
    );
  }
}

class _UserMenu extends ConsumerWidget {
  const _UserMenu({required this.user});
  final AuthenticatedUser user;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return PopupMenuButton<String>(
      tooltip: 'Conta',
      onSelected: (value) {
        if (value == 'profile') context.go('/perfil');
        if (value == 'logout') {
          ref.read(authControllerProvider.notifier).logout();
        }
      },
      itemBuilder: (context) => [
        PopupMenuItem(
          enabled: false,
          child: Text(user.email, overflow: TextOverflow.ellipsis),
        ),
        const PopupMenuItem(value: 'profile', child: Text('Meu perfil')),
        const PopupMenuItem(value: 'logout', child: Text('Sair')),
      ],
      child: Padding(
        padding: const EdgeInsets.symmetric(horizontal: 16),
        child: CircleAvatar(child: Text(_initial(user.name))),
      ),
    );
  }
}

String _initial(String name) {
  final normalized = name.trim();
  return normalized.isEmpty ? '?' : normalized.substring(0, 1).toUpperCase();
}

List<_Destination> _destinations(AuthenticatedUser user) {
  final platform = AppPlatformInfo.current;

  bool allows(AppCapability capability) {
    return AccessPolicy.allows(user, platform, capability);
  }

  return [
    if (allows(AppCapability.viewDashboard))
      const _Destination(
        '/dashboard',
        'Dashboard',
        Icons.dashboard_outlined,
        Icons.dashboard,
      ),
    if (allows(AppCapability.viewAllAppointments) ||
        allows(AppCapability.viewOwnAppointments))
      const _Destination(
        '/agendamentos',
        'Agendamentos',
        Icons.calendar_month_outlined,
        Icons.calendar_month,
      ),
    if (allows(AppCapability.createOwnAppointment))
      const _Destination(
        '/novo-agendamento',
        'Novo agendamento',
        Icons.add_circle_outline,
        Icons.add_circle,
      ),
    if (allows(AppCapability.evaluateAppointments))
      const _Destination(
        '/avaliacoes',
        'Avaliações',
        Icons.star_outline,
        Icons.star,
      ),
    if (allows(AppCapability.viewCatalog))
      const _Destination(
        '/catalogo',
        'Cursos e serviços',
        Icons.menu_book_outlined,
        Icons.menu_book,
      ),
    if (allows(AppCapability.viewClients) ||
        allows(AppCapability.viewStudents) ||
        allows(AppCapability.viewEmployees))
      const _Destination(
        '/pessoas',
        'Pessoas',
        Icons.groups_outlined,
        Icons.groups,
      ),
    if (allows(AppCapability.editProfile))
      const _Destination(
        '/perfil',
        'Meu perfil',
        Icons.person_outline,
        Icons.person,
      ),
  ];
}

class _Destination {
  const _Destination(this.path, this.label, this.icon, this.selectedIcon);
  final String path;
  final String label;
  final IconData icon;
  final IconData selectedIcon;
}
