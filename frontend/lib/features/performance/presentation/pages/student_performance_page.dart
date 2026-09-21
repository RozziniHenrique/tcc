import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/providers/app_providers.dart';
import '../../../../core/utils/date_periods.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/page_header.dart';
import '../../../management/data/repositories/management_repository.dart';
import '../../data/models/student_performance.dart';

class StudentPerformancePage extends ConsumerStatefulWidget {
  const StudentPerformancePage({super.key});

  @override
  ConsumerState<StudentPerformancePage> createState() =>
      _StudentPerformancePageState();
}

class _StudentPerformancePageState
    extends ConsumerState<StudentPerformancePage> {
  late DateTime _start;
  late DateTime _end;
  int? _courseId;
  int? _studentId;
  _PerformancePeriod _period = _PerformancePeriod.monthly;

  @override
  void initState() {
    super.initState();
    final period = DatePeriods.monthly(DateTime.now());
    _start = period.start;
    _end = period.end;
  }

  @override
  Widget build(BuildContext context) {
    final query = PerformanceQuery(
      start: _start,
      end: _end,
      courseId: _courseId,
      studentId: _studentId,
    );
    final result = ref.watch(studentPerformanceProvider(query));
    final courses = ref.watch(coursesProvider);
    final students = ref.watch(peopleProvider(PeopleResource.students));

    return PageBody(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const PageHeader(
            title: 'Desempenho dos alunos',
            subtitle: 'Acompanhe produtividade, cancelamentos e avaliações por período.',
          ),
          const SizedBox(height: 16),
          Card(
            child: Padding(
              padding: const EdgeInsets.all(16),
              child: Wrap(
                spacing: 12,
                runSpacing: 12,
                crossAxisAlignment: WrapCrossAlignment.center,
                children: [
                  for (final period in _PerformancePeriod.values)
                    ChoiceChip(
                      label: Text(period.label),
                      selected: _period == period,
                      onSelected: (_) => _selectPeriod(period),
                    ),
                  OutlinedButton.icon(
                    onPressed: _pickPeriod,
                    icon: const Icon(Icons.date_range_outlined),
                    label: Text('${formatDate(_start)} – ${formatDate(_end)}'),
                  ),
                  _Dropdown(
                    label: 'Curso',
                    value: _courseId,
                    items: courses.value
                        ?.map((item) => _Item(item.id, item.name))
                        .toList(),
                    onChanged: (value) => setState(() => _courseId = value),
                  ),
                  _Dropdown(
                    label: 'Aluno',
                    value: _studentId,
                    items: students.value
                        ?.map((item) => _Item(item.id, item.name))
                        .toList(),
                    onChanged: (value) => setState(() => _studentId = value),
                  ),
                  TextButton.icon(
                    onPressed: () {
                      final period = DatePeriods.monthly(DateTime.now());
                      setState(() {
                        _start = period.start;
                        _end = period.end;
                        _courseId = null;
                        _studentId = null;
                        _period = _PerformancePeriod.monthly;
                      });
                    },
                    icon: const Icon(Icons.filter_alt_off_outlined),
                    label: const Text('Limpar filtros'),
                  ),
                ],
              ),
            ),
          ),
          const SizedBox(height: 24),
          result.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (error, _) => Card(
              child: Padding(
                padding: const EdgeInsets.all(24),
                child: Column(
                  children: [
                    Text(ApiException.messageFor(error)),
                    const SizedBox(height: 8),
                    TextButton(
                      onPressed: () =>
                          ref.invalidate(studentPerformanceProvider(query)),
                      child: const Text('Tentar novamente'),
                    ),
                  ],
                ),
              ),
            ),
            data: (items) => items.isEmpty
                ? const Card(
                    child: Padding(
                      padding: EdgeInsets.all(32),
                      child: Text(
                        'Nenhum desempenho encontrado no período.',
                        textAlign: TextAlign.center,
                      ),
                    ),
                  )
                : _PerformanceContent(items: items),
          ),
        ],
      ),
    );
  }

  Future<void> _pickPeriod() async {
    final result = await showDateRangePicker(
      context: context,
      firstDate: DateTime(2020),
      lastDate: DateTime.now().add(const Duration(days: 730)),
      initialDateRange: DateTimeRange(start: _start, end: _end),
    );
    if (result != null) {
      setState(() {
        _start = result.start;
        _end = result.end;
        _period = _PerformancePeriod.custom;
      });
    }
  }

  void _selectPeriod(_PerformancePeriod selected) {
    if (selected == _PerformancePeriod.custom) {
      _pickPeriod();
      return;
    }

    final now = DateTime.now();
    final period = switch (selected) {
      _PerformancePeriod.weekly => DatePeriods.weekly(now),
      _PerformancePeriod.monthly => DatePeriods.monthly(now),
      _PerformancePeriod.annual => DatePeriods.annual(now),
      _PerformancePeriod.custom => throw StateError('Período inválido'),
    };
    setState(() {
      _period = selected;
      _start = period.start;
      _end = period.end;
    });
  }
}

enum _PerformancePeriod {
  weekly('Semanal'),
  monthly('Mensal'),
  annual('Anual'),
  custom('Personalizado');

  const _PerformancePeriod(this.label);
  final String label;
}

class _PerformanceContent extends StatelessWidget {
  const _PerformanceContent({required this.items});
  final List<StudentPerformance> items;

  @override
  Widget build(BuildContext context) {
    if (MediaQuery.sizeOf(context).width < 760) {
      return Column(
        children: [
          for (final item in items)
            Card(
              child: ListTile(
                title: Text(item.studentName),
                subtitle: Text(
                  '${item.courseName}\n'
                  '${item.completedAppointments}/${item.totalAppointments} '
                  'concluídos • ${item.cancelledAppointments} cancelados\n'
                  'Nota ${item.averageRating.toStringAsFixed(1)} '
                  '(${item.ratingCount} avaliações)',
                ),
                isThreeLine: true,
                trailing: Text('${item.completionRate.toStringAsFixed(0)}%'),
              ),
            ),
        ],
      );
    }

    return Card(
      child: SingleChildScrollView(
        scrollDirection: Axis.horizontal,
        child: DataTable(
          columns: const [
            DataColumn(label: Text('Aluno')),
            DataColumn(label: Text('Curso')),
            DataColumn(label: Text('Agendamentos')),
            DataColumn(label: Text('Concluídos')),
            DataColumn(label: Text('Cancelados')),
            DataColumn(label: Text('Conclusão')),
            DataColumn(label: Text('Avaliação')),
          ],
          rows: [
            for (final item in items)
              DataRow(
                cells: [
                  DataCell(Text(item.studentName)),
                  DataCell(Text(item.courseName)),
                  DataCell(Text(item.totalAppointments.toString())),
                  DataCell(Text(item.completedAppointments.toString())),
                  DataCell(Text(item.cancelledAppointments.toString())),
                  DataCell(Text('${item.completionRate.toStringAsFixed(1)}%')),
                  DataCell(
                    Text(
                      '${item.averageRating.toStringAsFixed(1)} '
                      '(${item.ratingCount})',
                    ),
                  ),
                ],
              ),
          ],
        ),
      ),
    );
  }
}

class _Item {
  const _Item(this.id, this.label);
  final int id;
  final String label;
}

class _Dropdown extends StatelessWidget {
  const _Dropdown({
    required this.label,
    required this.value,
    required this.items,
    required this.onChanged,
  });

  final String label;
  final int? value;
  final List<_Item>? items;
  final ValueChanged<int?> onChanged;

  @override
  Widget build(BuildContext context) {
    return SizedBox(
      width: 220,
      child: DropdownButtonFormField<int>(
        initialValue: value,
        decoration: InputDecoration(labelText: label),
        items: [
          for (final item in items ?? const <_Item>[])
            DropdownMenuItem(value: item.id, child: Text(item.label)),
        ],
        onChanged: items == null ? null : onChanged,
      ),
    );
  }
}
