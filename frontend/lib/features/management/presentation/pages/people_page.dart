import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/providers/app_providers.dart';
import '../../../../core/widgets/page_header.dart';
import '../../data/models/person_summary.dart';
import '../../data/repositories/management_repository.dart';

class PeoplePage extends ConsumerWidget {
  const PeoplePage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return DefaultTabController(
      length: PeopleResource.values.length,
      child: PageBody(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const PageHeader(
              title: 'Pessoas',
              subtitle: 'Consulte e desative os cadastros do sistema.',
            ),
            const SizedBox(height: 16),
            TabBar(
              tabs: [
                for (final resource in PeopleResource.values)
                  Tab(text: resource.label),
              ],
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 600,
              child: TabBarView(
                children: [
                  for (final resource in PeopleResource.values)
                    _PeopleList(resource: resource),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _PeopleList extends ConsumerWidget {
  const _PeopleList({required this.resource});
  final PeopleResource resource;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final people = ref.watch(peopleProvider(resource));
    return people.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, _) => Center(child: Text(ApiException.messageFor(error))),
      data: (items) => items.isEmpty
          ? const Center(child: Text('Nenhum cadastro encontrado.'))
          : ListView.separated(
              itemCount: items.length,
              separatorBuilder: (_, _) => const SizedBox(height: 8),
              itemBuilder: (context, index) =>
                  _PersonTile(person: items[index], resource: resource),
            ),
    );
  }
}

class _PersonTile extends ConsumerWidget {
  const _PersonTile({required this.person, required this.resource});
  final PersonSummary person;
  final PeopleResource resource;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Card(
      child: ListTile(
        leading: CircleAvatar(
          child: Text(
            person.name.trim().isEmpty
                ? '?'
                : person.name.trim().substring(0, 1).toUpperCase(),
          ),
        ),
        title: Text(person.name),
        subtitle: Text(
          [
            person.email,
            person.phone,
            person.detail,
          ].whereType<String>().where((value) => value.isNotEmpty).join(' • '),
        ),
        trailing: IconButton(
          tooltip: 'Desativar',
          icon: const Icon(Icons.person_off_outlined),
          onPressed: () => _delete(context, ref),
        ),
      ),
    );
  }

  Future<void> _delete(BuildContext context, WidgetRef ref) async {
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Confirmar desativação'),
        content: Text('Deseja desativar o cadastro de ${person.name}?'),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context, false),
            child: const Text('Cancelar'),
          ),
          FilledButton(
            onPressed: () => Navigator.pop(context, true),
            child: const Text('Desativar'),
          ),
        ],
      ),
    );
    if (confirmed != true || !context.mounted) return;

    try {
      await ref.read(managementRepositoryProvider).delete(resource, person.id);
      ref.invalidate(peopleProvider(resource));
    } catch (error) {
      if (context.mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(ApiException.messageFor(error))));
      }
    }
  }
}
