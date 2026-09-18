import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';
import 'package:go_router/go_router.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/providers/app_providers.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/page_header.dart';
import '../../../auth/presentation/controllers/auth_controller.dart';

class NewAppointmentPage extends ConsumerStatefulWidget {
  const NewAppointmentPage({super.key});

  @override
  ConsumerState<NewAppointmentPage> createState() => _NewAppointmentPageState();
}

class _NewAppointmentPageState extends ConsumerState<NewAppointmentPage> {
  final _formKey = GlobalKey<FormState>();
  int? _courseId;
  int? _unitId;
  final Set<int> _serviceIds = {};
  late DateTime _dateTime;
  bool _saving = false;

  @override
  void initState() {
    super.initState();
    final tomorrow = DateTime.now().add(const Duration(days: 1));
    _dateTime = DateTime(tomorrow.year, tomorrow.month, tomorrow.day, 9);
  }

  @override
  Widget build(BuildContext context) {
    final courses = ref.watch(coursesProvider);
    final units = ref.watch(unitsProvider);
    final services = ref.watch(servicesProvider);

    return PageBody(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const PageHeader(
            title: 'Novo agendamento',
            subtitle:
                'Escolha curso, unidade, serviços e um horário disponível.',
          ),
          const SizedBox(height: 24),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(24),
              child: Form(
                key: _formKey,
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.stretch,
                  children: [
                    courses.when(
                      loading: () => const LinearProgressIndicator(),
                      error: (error, _) => Text(ApiException.messageFor(error)),
                      data: (items) => DropdownButtonFormField<int>(
                        initialValue: _courseId,
                        decoration: const InputDecoration(labelText: 'Curso'),
                        items: [
                          for (final item in items)
                            DropdownMenuItem(
                              value: item.id,
                              child: Text(item.name),
                            ),
                        ],
                        onChanged: (value) => setState(() => _courseId = value),
                        validator: (value) =>
                            value == null ? 'Selecione um curso.' : null,
                      ),
                    ),
                    const SizedBox(height: 16),
                    units.when(
                      loading: () => const LinearProgressIndicator(),
                      error: (error, _) => Text(ApiException.messageFor(error)),
                      data: (items) => DropdownButtonFormField<int>(
                        initialValue: _unitId,
                        decoration: const InputDecoration(labelText: 'Unidade'),
                        items: [
                          for (final item in items)
                            DropdownMenuItem(
                              value: item.id,
                              child: Text(
                                '${item.name} — ${item.city}/${item.state}',
                              ),
                            ),
                        ],
                        onChanged: (value) => setState(() => _unitId = value),
                        validator: (value) =>
                            value == null ? 'Selecione uma unidade.' : null,
                      ),
                    ),
                    const SizedBox(height: 20),
                    Text(
                      'Serviços',
                      style: Theme.of(context).textTheme.titleMedium,
                    ),
                    const SizedBox(height: 8),
                    services.when(
                      loading: () => const LinearProgressIndicator(),
                      error: (error, _) => Text(ApiException.messageFor(error)),
                      data: (items) => Column(
                        children: [
                          for (final item in items)
                            CheckboxListTile(
                              contentPadding: EdgeInsets.zero,
                              value: _serviceIds.contains(item.id),
                              title: Text(item.name),
                              subtitle: Text(formatMoney(item.price)),
                              onChanged: (checked) {
                                setState(() {
                                  if (checked ?? false) {
                                    _serviceIds.add(item.id);
                                  } else {
                                    _serviceIds.remove(item.id);
                                  }
                                });
                              },
                            ),
                        ],
                      ),
                    ),
                    if (_serviceIds.isEmpty)
                      Text(
                        'Selecione pelo menos um serviço.',
                        style: TextStyle(
                          color: Theme.of(context).colorScheme.error,
                        ),
                      ),
                    const SizedBox(height: 16),
                    OutlinedButton.icon(
                      onPressed: _pickDateTime,
                      icon: const Icon(Icons.schedule),
                      label: Text(formatDateTime(_dateTime)),
                    ),
                    const SizedBox(height: 24),
                    FilledButton(
                      onPressed: _saving ? null : _submit,
                      child: _saving
                          ? const SizedBox.square(
                              dimension: 20,
                              child: CircularProgressIndicator(strokeWidth: 2),
                            )
                          : const Text('Confirmar agendamento'),
                    ),
                  ],
                ),
              ),
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _pickDateTime() async {
    final date = await showDatePicker(
      context: context,
      firstDate: DateTime.now(),
      lastDate: DateTime.now().add(const Duration(days: 365)),
      initialDate: _dateTime,
    );
    if (date == null || !mounted) return;

    final time = await showTimePicker(
      context: context,
      initialTime: TimeOfDay.fromDateTime(_dateTime),
    );
    if (time == null) return;

    setState(() {
      _dateTime = DateTime(
        date.year,
        date.month,
        date.day,
        time.hour,
        time.minute,
      );
    });
  }

  Future<void> _submit() async {
    if (!_formKey.currentState!.validate() || _serviceIds.isEmpty) {
      setState(() {});
      return;
    }
    setState(() => _saving = true);
    try {
      final user = ref.read(authControllerProvider).value!;
      await ref
          .read(appointmentRepositoryProvider)
          .create(
            clientId: user.id,
            courseId: _courseId!,
            unitId: _unitId!,
            serviceIds: _serviceIds.toList(),
            dateTime: _dateTime,
          );
      ref.invalidate(appointmentsProvider);
      if (mounted) context.go('/agendamentos');
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(ApiException.messageFor(error))));
      }
    } finally {
      if (mounted) setState(() => _saving = false);
    }
  }
}
