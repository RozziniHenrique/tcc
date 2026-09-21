import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/download/file_download.dart';
import '../../../../core/platform/app_platform.dart';
import '../../../../core/providers/app_providers.dart';
import '../../../../core/utils/date_periods.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/page_header.dart';
import '../../data/repositories/dashboard_repository.dart';

class DashboardPage extends ConsumerStatefulWidget {
  const DashboardPage({super.key});

  @override
  ConsumerState<DashboardPage> createState() => _DashboardPageState();
}

class _DashboardPageState extends ConsumerState<DashboardPage> {
  late DateTime _start;
  late DateTime _end;
  _PeriodPreset _preset = _PeriodPreset.monthly;
  ReportExportFormat? _exporting;

  @override
  void initState() {
    super.initState();
    final period = DatePeriods.monthly(DateTime.now());
    _start = period.start;
    _end = period.end;
  }

  @override
  Widget build(BuildContext context) {
    final period = ReportPeriod(_start, _end);
    final report = ref.watch(dashboardReportProvider(period));

    return PageBody(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          PageHeader(
            title: 'Dashboard',
            subtitle: 'Desempenho e faturamento do período selecionado.',
            action: AppPlatformInfo.current == AppPlatform.web
                ? _ExportMenu(exporting: _exporting, onSelected: _export)
                : null,
          ),
          const SizedBox(height: 16),
          _PeriodSelector(
            selected: _preset,
            start: _start,
            end: _end,
            onSelected: _selectPreset,
          ),
          const SizedBox(height: 24),
          report.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (error, _) => _ErrorCard(
              message: ApiException.messageFor(error),
              onRetry: () => ref.invalidate(dashboardReportProvider(period)),
            ),
            data: (data) => Column(
              crossAxisAlignment: CrossAxisAlignment.stretch,
              children: [
                GridView.count(
                  crossAxisCount: MediaQuery.sizeOf(context).width >= 900
                      ? 3
                      : 1,
                  childAspectRatio: 3.2,
                  crossAxisSpacing: 16,
                  mainAxisSpacing: 16,
                  shrinkWrap: true,
                  physics: const NeverScrollableScrollPhysics(),
                  children: [
                    _MetricCard(
                      label: 'Faturamento',
                      value: formatMoney(data.revenue),
                      icon: Icons.payments_outlined,
                    ),
                    _MetricCard(
                      label: 'Agendamentos',
                      value: data.totalAppointments.toString(),
                      icon: Icons.event_available_outlined,
                    ),
                    _MetricCard(
                      label: 'Cursos com movimento',
                      value: data.appointmentsByCourse.length.toString(),
                      icon: Icons.school_outlined,
                    ),
                  ],
                ),
                const SizedBox(height: 24),
                _CourseTable(
                  title: 'Agendamentos por curso',
                  rows: data.appointmentsByCourse
                      .map(
                        (item) => [
                          item.courseName,
                          item.quantity.toString(),
                          formatMoney(item.amount ?? 0),
                        ],
                      )
                      .toList(),
                  columns: const ['Curso', 'Agendamentos', 'Faturamento'],
                ),
                const SizedBox(height: 16),
                _CourseTable(
                  title: 'Alunos por curso',
                  rows: data.studentsByCourse
                      .map(
                        (item) => [item.courseName, item.quantity.toString()],
                      )
                      .toList(),
                  columns: const ['Curso', 'Alunos'],
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }

  Future<void> _export(ReportExportFormat format) async {
    setState(() => _exporting = format);
    try {
      final bytes = await ref
          .read(dashboardRepositoryProvider)
          .export(format, _start, _end);
      downloadFile(
        bytes,
        'relatorio-stfer-${_apiDate(_start)}-${_apiDate(_end)}.'
        '${format.extension}',
        format.contentType,
      );
    } catch (error) {
      if (mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(ApiException.messageFor(error))));
      }
    } finally {
      if (mounted) setState(() => _exporting = null);
    }
  }

  String _apiDate(DateTime value) {
    String two(int number) => number.toString().padLeft(2, '0');
    return '${value.year}-${two(value.month)}-${two(value.day)}';
  }

  Future<void> _selectPreset(_PeriodPreset preset) async {
    if (preset == _PeriodPreset.custom) {
      final result = await showDialog<DateTimeRange>(
        context: context,
        builder: (context) =>
            _DesktopDateRangeDialog(initialStart: _start, initialEnd: _end),
      );
      if (result == null || !mounted) return;
      setState(() {
        _preset = preset;
        _start = result.start;
        _end = result.end;
      });
      return;
    }

    final now = DateTime.now();
    final period = switch (preset) {
      _PeriodPreset.weekly => DatePeriods.weekly(now),
      _PeriodPreset.monthly => DatePeriods.monthly(now),
      _PeriodPreset.annual => DatePeriods.annual(now),
      _PeriodPreset.custom => throw StateError(
        'Período personalizado inválido',
      ),
    };

    setState(() {
      _preset = preset;
      _start = period.start;
      _end = period.end;
    });
  }
}

class _ExportMenu extends StatelessWidget {
  const _ExportMenu({required this.exporting, required this.onSelected});

  final ReportExportFormat? exporting;
  final ValueChanged<ReportExportFormat> onSelected;

  @override
  Widget build(BuildContext context) {
    if (exporting != null) {
      return const SizedBox.square(
        dimension: 32,
        child: CircularProgressIndicator(strokeWidth: 2),
      );
    }

    return PopupMenuButton<ReportExportFormat>(
      tooltip: 'Exportar relatório',
      onSelected: onSelected,
      itemBuilder: (context) => const [
        PopupMenuItem(
          value: ReportExportFormat.csv,
          child: Text('Exportar CSV'),
        ),
        PopupMenuItem(
          value: ReportExportFormat.pdf,
          child: Text('Exportar PDF'),
        ),
        PopupMenuItem(
          value: ReportExportFormat.xlsx,
          child: Text('Exportar Excel'),
        ),
      ],
      child: const Chip(
        avatar: Icon(Icons.download_outlined, size: 18),
        label: Text('Exportar'),
      ),
    );
  }
}

enum _PeriodPreset {
  weekly('Semanal'),
  monthly('Mensal'),
  annual('Anual'),
  custom('Personalizado');

  const _PeriodPreset(this.label);
  final String label;
}

class _PeriodSelector extends StatelessWidget {
  const _PeriodSelector({
    required this.selected,
    required this.start,
    required this.end,
    required this.onSelected,
  });

  final _PeriodPreset selected;
  final DateTime start;
  final DateTime end;
  final ValueChanged<_PeriodPreset> onSelected;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(16),
        child: Wrap(
          crossAxisAlignment: WrapCrossAlignment.center,
          spacing: 10,
          runSpacing: 12,
          children: [
            Text('Período:', style: Theme.of(context).textTheme.titleMedium),
            for (final preset in _PeriodPreset.values)
              ChoiceChip(
                label: Text(preset.label),
                selected: selected == preset,
                onSelected: (_) => onSelected(preset),
              ),
            const SizedBox(width: 8),
            Chip(
              avatar: const Icon(Icons.date_range, size: 18),
              label: Text('${formatDate(start)} – ${formatDate(end)}'),
            ),
          ],
        ),
      ),
    );
  }
}

class _DesktopDateRangeDialog extends StatefulWidget {
  const _DesktopDateRangeDialog({
    required this.initialStart,
    required this.initialEnd,
  });

  final DateTime initialStart;
  final DateTime initialEnd;

  @override
  State<_DesktopDateRangeDialog> createState() =>
      _DesktopDateRangeDialogState();
}

class _DesktopDateRangeDialogState extends State<_DesktopDateRangeDialog> {
  late DateTime _start;
  late DateTime _end;

  @override
  void initState() {
    super.initState();
    _start = widget.initialStart;
    _end = widget.initialEnd;
  }

  @override
  Widget build(BuildContext context) {
    final firstDate = DateTime(2020);
    final lastDate = DateTime.now().add(const Duration(days: 730));

    return Dialog(
      child: ConstrainedBox(
        constraints: const BoxConstraints(maxWidth: 900, maxHeight: 720),
        child: Padding(
          padding: const EdgeInsets.all(24),
          child: Column(
            mainAxisSize: MainAxisSize.min,
            crossAxisAlignment: CrossAxisAlignment.stretch,
            children: [
              Text(
                'Selecionar período',
                style: Theme.of(context).textTheme.headlineSmall,
              ),
              const SizedBox(height: 4),
              const Text(
                'Navegue pelos meses e escolha as datas inicial e final.',
              ),
              const SizedBox(height: 16),
              Flexible(
                child: LayoutBuilder(
                  builder: (context, constraints) {
                    final calendars = [
                      _CalendarPanel(
                        title: 'Data inicial',
                        selectedDate: _start,
                        firstDate: firstDate,
                        lastDate: _end,
                        onChanged: (date) => setState(() => _start = date),
                      ),
                      _CalendarPanel(
                        key: ValueKey(_start),
                        title: 'Data final',
                        selectedDate: _end.isBefore(_start) ? _start : _end,
                        firstDate: _start,
                        lastDate: lastDate,
                        onChanged: (date) => setState(() => _end = date),
                      ),
                    ];

                    if (constraints.maxWidth >= 720) {
                      return Row(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Expanded(child: calendars[0]),
                          const SizedBox(width: 20),
                          Expanded(child: calendars[1]),
                        ],
                      );
                    }

                    return SingleChildScrollView(
                      child: Column(
                        children: [
                          calendars[0],
                          const SizedBox(height: 16),
                          calendars[1],
                        ],
                      ),
                    );
                  },
                ),
              ),
              const SizedBox(height: 16),
              Wrap(
                alignment: WrapAlignment.spaceBetween,
                crossAxisAlignment: WrapCrossAlignment.center,
                spacing: 16,
                runSpacing: 12,
                children: [
                  Text(
                    '${formatDate(_start)} – ${formatDate(_end)}',
                    style: Theme.of(context).textTheme.titleMedium,
                  ),
                  Row(
                    mainAxisSize: MainAxisSize.min,
                    children: [
                      TextButton(
                        onPressed: () => Navigator.pop(context),
                        child: const Text('Cancelar'),
                      ),
                      const SizedBox(width: 8),
                      FilledButton(
                        onPressed: () => Navigator.pop(
                          context,
                          DateTimeRange(start: _start, end: _end),
                        ),
                        child: const Text('Aplicar'),
                      ),
                    ],
                  ),
                ],
              ),
            ],
          ),
        ),
      ),
    );
  }
}

class _CalendarPanel extends StatelessWidget {
  const _CalendarPanel({
    required this.title,
    required this.selectedDate,
    required this.firstDate,
    required this.lastDate,
    required this.onChanged,
    super.key,
  });

  final String title;
  final DateTime selectedDate;
  final DateTime firstDate;
  final DateTime lastDate;
  final ValueChanged<DateTime> onChanged;

  @override
  Widget build(BuildContext context) {
    return Card.outlined(
      child: Padding(
        padding: const EdgeInsets.all(12),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Padding(
              padding: const EdgeInsets.fromLTRB(12, 4, 12, 0),
              child: Text(
                title,
                style: Theme.of(context).textTheme.titleMedium,
              ),
            ),
            CalendarDatePicker(
              initialDate: selectedDate,
              firstDate: firstDate,
              lastDate: lastDate,
              onDateChanged: onChanged,
            ),
          ],
        ),
      ),
    );
  }
}

class _MetricCard extends StatelessWidget {
  const _MetricCard({
    required this.label,
    required this.value,
    required this.icon,
  });
  final String label;
  final String value;
  final IconData icon;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Row(
          children: [
            CircleAvatar(child: Icon(icon)),
            const SizedBox(width: 16),
            Expanded(
              child: Column(
                mainAxisSize: MainAxisSize.min,
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text(label),
                  Text(value, style: Theme.of(context).textTheme.headlineSmall),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _CourseTable extends StatelessWidget {
  const _CourseTable({
    required this.title,
    required this.rows,
    required this.columns,
  });
  final String title;
  final List<List<String>> rows;
  final List<String> columns;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(20),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text(title, style: Theme.of(context).textTheme.titleLarge),
            const SizedBox(height: 12),
            if (rows.isEmpty)
              const Text('Nenhum resultado no período.')
            else
              SingleChildScrollView(
                scrollDirection: Axis.horizontal,
                child: DataTable(
                  columns: [
                    for (final column in columns)
                      DataColumn(label: Text(column)),
                  ],
                  rows: [
                    for (final row in rows)
                      DataRow(
                        cells: [for (final value in row) DataCell(Text(value))],
                      ),
                  ],
                ),
              ),
          ],
        ),
      ),
    );
  }
}

class _ErrorCard extends StatelessWidget {
  const _ErrorCard({required this.message, required this.onRetry});
  final String message;
  final VoidCallback onRetry;

  @override
  Widget build(BuildContext context) {
    return Card(
      child: Padding(
        padding: const EdgeInsets.all(24),
        child: Column(
          children: [
            Text(message, textAlign: TextAlign.center),
            const SizedBox(height: 12),
            FilledButton.tonal(
              onPressed: onRetry,
              child: const Text('Tentar novamente'),
            ),
          ],
        ),
      ),
    );
  }
}
