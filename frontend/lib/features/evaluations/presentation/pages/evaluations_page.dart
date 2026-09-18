import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../../../core/network/api_exception.dart';
import '../../../../core/providers/app_providers.dart';
import '../../../../core/utils/formatters.dart';
import '../../../../core/widgets/page_header.dart';
import '../../../appointments/data/models/appointment.dart';

class EvaluationsPage extends ConsumerWidget {
  const EvaluationsPage({super.key});

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    final pending = ref.watch(pendingEvaluationsProvider);
    return PageBody(
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const PageHeader(
            title: 'Avaliações pendentes',
            subtitle: 'Avalie os atendimentos que já foram concluídos.',
          ),
          const SizedBox(height: 24),
          pending.when(
            loading: () => const Center(child: CircularProgressIndicator()),
            error: (error, _) =>
                Center(child: Text(ApiException.messageFor(error))),
            data: (items) => items.isEmpty
                ? const Card(
                    child: Padding(
                      padding: EdgeInsets.all(32),
                      child: Text(
                        'Você não possui avaliações pendentes.',
                        textAlign: TextAlign.center,
                      ),
                    ),
                  )
                : ListView.separated(
                    shrinkWrap: true,
                    physics: const NeverScrollableScrollPhysics(),
                    itemCount: items.length,
                    separatorBuilder: (_, _) => const SizedBox(height: 12),
                    itemBuilder: (context, index) =>
                        _EvaluationCard(item: items[index]),
                  ),
          ),
        ],
      ),
    );
  }
}

class _EvaluationCard extends ConsumerWidget {
  const _EvaluationCard({required this.item});
  final PendingEvaluation item;

  @override
  Widget build(BuildContext context, WidgetRef ref) {
    return Card(
      child: ListTile(
        title: Text(item.courseName),
        subtitle: Text(
          '${item.studentName} • ${formatDateTime(item.dateTime)}',
        ),
        trailing: FilledButton.tonal(
          onPressed: () => _openForm(context, ref),
          child: const Text('Avaliar'),
        ),
      ),
    );
  }

  Future<void> _openForm(BuildContext context, WidgetRef ref) async {
    var rating = 5;
    final comment = TextEditingController();
    final confirmed = await showDialog<bool>(
      context: context,
      builder: (context) => StatefulBuilder(
        builder: (context, setState) => AlertDialog(
          title: const Text('Avaliar atendimento'),
          content: Column(
            mainAxisSize: MainAxisSize.min,
            children: [
              Wrap(
                children: [
                  for (var index = 1; index <= 5; index++)
                    IconButton(
                      onPressed: () => setState(() => rating = index),
                      icon: Icon(
                        index <= rating ? Icons.star : Icons.star_border,
                        color: Colors.amber.shade700,
                      ),
                    ),
                ],
              ),
              TextField(
                controller: comment,
                maxLength: 500,
                maxLines: 4,
                decoration: const InputDecoration(
                  labelText: 'Comentário (opcional)',
                ),
              ),
            ],
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(context, false),
              child: const Text('Cancelar'),
            ),
            FilledButton(
              onPressed: () => Navigator.pop(context, true),
              child: const Text('Enviar'),
            ),
          ],
        ),
      ),
    );

    if (confirmed != true || !context.mounted) {
      comment.dispose();
      return;
    }

    try {
      await ref
          .read(evaluationRepositoryProvider)
          .submit(
            appointmentId: item.appointmentId,
            rating: rating,
            comment: comment.text.trim().isEmpty ? null : comment.text.trim(),
          );
      ref.invalidate(pendingEvaluationsProvider);
      if (context.mounted) {
        ScaffoldMessenger.of(context).showSnackBar(
          const SnackBar(content: Text('Avaliação enviada com sucesso.')),
        );
      }
    } catch (error) {
      if (context.mounted) {
        ScaffoldMessenger.of(
          context,
        ).showSnackBar(SnackBar(content: Text(ApiException.messageFor(error))));
      }
    } finally {
      comment.dispose();
    }
  }
}
