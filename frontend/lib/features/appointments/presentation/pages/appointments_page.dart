import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/providers/app_providers.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/page_header.dart';
import '../../../auth/data/models/authenticated_user.dart';
import '../../../auth/presentation/controllers/auth_controller.dart';
import '../../../management/data/repositories/management_repository.dart';
import '../../data/models/appointment.dart';

class AppointmentsPage extends ConsumerStatefulWidget {
  const AppointmentsPage({super.key});

  @override
  ConsumerState<AppointmentsPage> createState() => _AppointmentsPageState();
}

class _AppointmentsPageState extends ConsumerState<AppointmentsPage> {
  AppointmentFilters _filters = const AppointmentFilters();

  @override
  Widget build(BuildContext context) {
    final user = ref.watch(authControllerProvider).value!;
    final appointments = ref.watch(appointmentsProvider(_filters));
    final employee = user.hasProfile(UserProfile.employee);

    return PageBody(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          PageHeader(
            title: 'Agendamentos',
            subtitle: user.hasProfile(UserProfile.employee)
                ? 'Consulte e acompanhe a agenda da operação.'
                : 'Acompanhe seus horários e atendimentos.',
          ),
          const SizedBox(height: 16),
          _FiltersCard(
            filters: _filters,
            employee: employee,
            onChanged: (value) => setState(() => _filters = value),
          ),
          const SizedBox(height: 24),
          appointments.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (error, _) => _MessageCard(
              message: ApiException.messageFor(error),
              action: TextButton(
                onPressed: () => ref.invalidate(appointmentsProvider(_filters)),
                child: const Text('Tentar novamente'),
              ),
            ),
            data: (items) => items.isEmpty
                ? const _MessageCard(message: 'Nenhum agendamento encontrado.')
                : ListView.separated(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: items.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 12),
                    itemBuilder: (context, index) =>
                        _AppointmentCard(item: items[index], user: user),
                  ),
          ),
        ],
      ),
    );
  }
}

class _FiltersCard extends ConsumerWidget {
  const _FiltersCard({
    required this.filters,
    required this.employee,
    required this.onChanged,
  });

  final AppointmentFilters filters;
  final bool employee;
  final ValueChanged<AppointmentFilters> onChanged;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final courses = employee ? ref.watch(coursesProvider) : null;
    final units = employee ? ref.watch(unitsProvider) : null;
    final students = employee
        ? ref.watch(peopleProvider(PeopleResource.students))
        : null;
    final clients = employee
        ? ref.watch(peopleProvider(PeopleResource.clients))
        : null;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            Wrap(
              spacing: 12,
              runSpacing: 12,
              crossAxisAlignment: WrapCrossAlignment.center,
              children: [
                SizedBox(
                  width: 210,
                  child: DropdownButtonFormField<AppointmentStatus>(
                    initialValue: filters.status,
                    decoration: const InputDecoration(labelText: 'Status'),
                    items: [
                      for (final status in AppointmentStatus.values)
                        DropdownMenuItem(
                          value: status,
                          child: Text(status.label),
                        ),
                    ],
                    onChanged: (value) => onChanged(
                      filters.copyWith(
                        status: value,
                        clearStatus: value == null,
                      ),
                    ),
                  ),
                ),
                OutlinedButton.icon(
                  onPressed: () => _pickPeriod(context),
                  icon: const Icon(Icons.date_range_outlined),
                  label: Text(
                    filters.start == null || filters.end == null
                        ? 'Selecionar período'
                        : '${formatDate(filters.start!)} – '
                              '${formatDate(filters.end!)}',
                  ),
                ),
                if (employee) ...[
                  _AsyncDropdown(
                    label: 'Curso',
                    value: filters.courseId,
                    items: courses?.value
                        ?.map((item) => _FilterItem(item.id, item.name))
                        .toList(),
                    onChanged: (value) => onChanged(
                      filters.copyWith(
                        courseId: value,
                        clearCourse: value == null,
                      ),
                    ),
                  ),
                  _AsyncDropdown(
                    label: 'Unidade',
                    value: filters.unitId,
                    items: units?.value
                        ?.map((item) => _FilterItem(item.id, item.name))
                        .toList(),
                    onChanged: (value) => onChanged(
                      filters.copyWith(unitId: value, clearUnit: value == null),
                    ),
                  ),
                  _AsyncDropdown(
                    label: 'Aluno',
                    value: filters.studentId,
                    items: students?.value
                        ?.map((item) => _FilterItem(item.id, item.name))
                        .toList(),
                    onChanged: (value) => onChanged(
                      filters.copyWith(
                        studentId: value,
                        clearStudent: value == null,
                      ),
                    ),
                  ),
                  _AsyncDropdown(
                    label: 'Cliente',
                    value: filters.clientId,
                    items: clients?.value
                        ?.map((item) => _FilterItem(item.id, item.name))
                        .toList(),
                    onChanged: (value) => onChanged(
                      filters.copyWith(
                        clientId: value,
                        clearClient: value == null,
                      ),
                    ),
                  ),
                ],
                TextButton.icon(
                  onPressed: () => onChanged(const AppointmentFilters()),
                  icon: const Icon(Icons.filter_alt_off_outlined),
                  label: const Text('Limpar filtros'),
                ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _pickPeriod(BuildContext context) async {
    final now = DateTime.now();
    final result = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2020),
      lastDate: now.add(const Duration(days: 730)),
      initialDateRange: filters.start == null || filters.end == null
          ? null
          : DateTimeRange(start: filters.start!, end: filters.end!),
    );
    if (result != null) {
      onChanged(filters.copyWith(start: result.start, end: result.end));
    }
  }
}

class _FilterItem {
  const _FilterItem(this.id, this.label);
  final int id;
  final String label;
}

class _AsyncDropdown extends StatelessWidget {
  const _AsyncDropdown({
    required this.label,
    required this.value,
    required this.items,
    required this.onChanged,
  });

  final String label;
  final int? value;
  final List<_FilterItem>? items;
  final ValueChanged<int?> onChanged;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 210,
      child: DropdownButtonFormField<int>(
        initialValue: value,
        decoration: InputDecoration(labelText: label),
        items: [
          for (final item in items ?? const <_FilterItem>[])
            DropdownMenuItem(value: item.id, child: Text(item.label)),
        ],
        onChanged: items == null ? null : onChanged,
      ),
    );
  }
}

class _AppointmentCard extends ConsumerWidget {
  const _AppointmentCard({required this.item, required this.user});
  final Appointment item;
  final AuthenticatedUser user;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final canComplete =
        (user.hasProfile(UserProfile.employee) ||
            user.hasProfile(UserProfile.student)) &&
        item.status == AppointmentStatus.scheduled;
    final canCancel =
        (user.hasProfile(UserProfile.employee) ||
            user.hasProfile(UserProfile.client)) &&
        item.status == AppointmentStatus.scheduled;

    return Card(
      child: Padding(
        padding: const EdgeInsets.all(18),
        child: Wrap(
          alignment: WrapAlignment.spaceBetween,
          crossAxisAlignment: WrapCrossAlignment.center,
          spacing: 16,
          runSpacing: 12,
          children: [
            ConstrainedBox(
              constraints: const BoxConstraints(maxWidth: 650),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      Flexible(
                        child: Text(
                          item.courseName,
                          style: Theme.of(context).textTheme.titleMedium,
                        ),
                      ),
                      const SizedBox(width: 10),
                      Chip(label: Text(item.status.label)),
                    ],
                  ),
                  const SizedBox(height: 8),
                  Text(formatDateTime(item.dateTime)),
                  Text('${item.unitName} • ${item.studentName}'),
                  if (user.hasProfile(UserProfile.employee))
                    Text('Cliente: ${item.clientName}'),
                  Text(formatMoney(item.amount)),
                ],
              ),
            ),
            Wrap(
              spacing: 8,
              children: [
                if (canComplete)
                  FilledButton.tonal(
                    onPressed: () => _complete(context, ref),
                    child: const Text('Concluir'),
                  ),
                if (canCancel)
                  OutlinedButton(
                    onPressed: () => _cancel(context, ref),
                    child: const Text('Cancelar'),
                  ),
              ],
            ),
          ],
        ),
      ),
    );
  }

  Future<void> _complete(BuildContext context, WidgetRef ref) async {
    try {
      await ref.read(appointmentRepositoryProvider).complete(item.id);
      ref.invalidate(appointmentsProvider);
    } catch (error) {
      if (context.mounted) _showError(context, error);
    }
  }

  Future<void> _cancel(BuildContext context, WidgetRef ref) async {
    final controller = TextEditingController();
    final reason = await showDialog<String>(
      context: context,
      builder: (context) => AlertDialog(
        title: const Text('Cancelar agendamento'),
        content: TextField(
          controller: controller,
          maxLength: 255,
          maxLines: 3,
          decoration: const InputDecoration(labelText: 'Justificativa'),
        ),
        actions: [
          TextButton(
            onPressed: () => Navigator.pop(context),
            child: const Text('Voltar'),
          ),
          FilledButton(
            onPressed: () {
              final value = controller.text.trim();
              if (value.isNotEmpty) Navigator.pop(context, value);
            },
            child: const Text('Confirmar'),
          ),
        ],
      ),
    );
    controller.dispose();
    if (reason == null || !context.mounted) return;

    try {
      await ref.read(appointmentRepositoryProvider).cancel(item.id, reason);
      ref.invalidate(appointmentsProvider);
    } catch (error) {
      if (context.mounted) _showError(context, error);
    }
  }

  void _showError(BuildContext context, Object error) {
    ScaffoldMessenger.of(context)
        .showSnackBar(SnackBar(content: Text(ApiException.messageFor(error))));
  }
}

class _MessageCard extends StatelessWidget {
  const _MessageCard({required this.message, this.action});
  final String message;
  final Widget? action;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(32),
        child: Column(
          children: [
            Text(message, textAlign: TextAlign.center),
            ?action,
          ],
        ),
      ),
    );
  }
}
