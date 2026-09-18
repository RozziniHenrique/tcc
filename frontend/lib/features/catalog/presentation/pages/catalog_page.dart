import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/providers/app_providers.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/page_header.dart';
import '../../data/models/catalog_models.dart';

class CatalogPage extends ConsumerWidget {
  const CatalogPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return DefaultTabController(
      length: 3,
      child: PageBody(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            const PageHeader(
              title: 'Catálogo',
              subtitle: 'Consulte cursos, serviços e unidades disponíveis.',
            ),
            const SizedBox(height: 16),
            const TabBar(
              tabs: [
                Tab(text: 'Cursos'),
                Tab(text: 'Serviços'),
                Tab(text: 'Unidades'),
              ],
            ),
            const SizedBox(height: 16),
            SizedBox(
              height: 600,
              child: TabBarView(
                children: [
                  _CatalogList(asyncItems: ref.watch(coursesProvider)),
                  _CatalogList(asyncItems: ref.watch(servicesProvider)),
                  _CatalogList(asyncItems: ref.watch(unitsProvider)),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CatalogList<T extends CatalogEntity> extends StatelessWidget {
  const _CatalogList({required this.asyncItems});
  final AsyncValue<List<T>> asyncItems;

  @override
  Widget build(BuildContext context) {
    return asyncItems.when(
      loading: () => const Center(child: CircularProgressIndicator()),
      error: (error, _) => Center(child: Text(ApiException.messageFor(error))),
      data: (items) => items.isEmpty
          ? const Center(child: Text('Nenhum item disponível.'))
          : ListView.separated(
              itemCount: items.length,
              separatorBuilder: (_, _) => const SizedBox(height: 8),
              itemBuilder: (context, index) {
                final item = items[index];
                final trailing = switch (item) {
                  Course course => formatMoney(course.price),
                  ServiceItem service => formatMoney(service.price),
                  _ => null,
                };
                return Card(
                  child: ListTile(
                    title: Text(item.title),
                    subtitle: item.subtitle.isEmpty
                        ? null
                        : Text(item.subtitle),
                    trailing: trailing == null ? null : Text(trailing),
                  ),
                );
              },
            ),
    );
  }
}
