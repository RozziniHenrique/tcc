import 'package:flutter_riverpod/flutter_riverpod.dart';

import '../../features/appointments/data/models/appointment.dart';
import '../../features/appointments/data/repositories/appointment_repository.dart';
import '../../features/auth/presentation/controllers/auth_controller.dart';
import '../../features/catalog/data/models/catalog_models.dart';
import '../../features/catalog/data/repositories/catalog_repository.dart';
import '../../features/dashboard/data/models/dashboard_report.dart';
import '../../features/dashboard/data/repositories/dashboard_repository.dart';
import '../../features/evaluations/data/repositories/evaluation_repository.dart';
import '../../features/management/data/models/person_summary.dart';
import '../../features/management/data/repositories/management_repository.dart';
import '../../features/profile/data/repositories/profile_repository.dart';
import '../../features/performance/data/models/student_performance.dart';
import '../../features/performance/data/repositories/performance_repository.dart';

final catalogRepositoryProvider = Provider<CatalogRepository>(
  (ref) => CatalogRepository(ref.watch(apiClientProvider)),
);
final appointmentRepositoryProvider = Provider<AppointmentRepository>(
  (ref) => AppointmentRepository(ref.watch(apiClientProvider)),
);
final evaluationRepositoryProvider = Provider<EvaluationRepository>(
  (ref) => EvaluationRepository(ref.watch(apiClientProvider)),
);
final dashboardRepositoryProvider = Provider<DashboardRepository>(
  (ref) => DashboardRepository(ref.watch(apiClientProvider)),
);
final managementRepositoryProvider = Provider<ManagementRepository>(
  (ref) => ManagementRepository(ref.watch(apiClientProvider)),
);
final profileRepositoryProvider = Provider<ProfileRepository>(
  (ref) => ProfileRepository(ref.watch(apiClientProvider)),
);
final performanceRepositoryProvider = Provider<PerformanceRepository>(
  (ref) => PerformanceRepository(ref.watch(apiClientProvider)),
);

final coursesProvider = FutureProvider.autoDispose<List<Course>>(
  (ref) => ref.watch(catalogRepositoryProvider).courses(),
);
final servicesProvider = FutureProvider.autoDispose<List<ServiceItem>>(
  (ref) => ref.watch(catalogRepositoryProvider).services(),
);
final unitsProvider = FutureProvider.autoDispose<List<UnitItem>>(
  (ref) => ref.watch(catalogRepositoryProvider).units(),
);

final appointmentsProvider = FutureProvider.autoDispose
    .family<List<Appointment>, AppointmentFilters>(
      (ref, filters) =>
          ref.watch(appointmentRepositoryProvider).list(filters: filters),
    );

final availabilityProvider = FutureProvider.autoDispose
    .family<List<AvailableSlot>, AvailabilityQuery>(
      (ref, query) => ref
          .watch(appointmentRepositoryProvider)
          .availability(courseId: query.courseId, date: query.date),
    );

final pendingEvaluationsProvider =
    FutureProvider.autoDispose<List<PendingEvaluation>>(
      (ref) => ref.watch(evaluationRepositoryProvider).pending(),
    );

class ReportPeriod {
  const ReportPeriod(this.start, this.end);
  final DateTime start;
  final DateTime end;

  @override
  bool operator ==(Object other) =>
      other is ReportPeriod && other.start == start && other.end == end;

  @override
  int get hashCode => Object.hash(start, end);
}

final dashboardReportProvider = FutureProvider.autoDispose
    .family<DashboardReport, ReportPeriod>(
      (ref, period) => ref
          .watch(dashboardRepositoryProvider)
          .report(period.start, period.end),
    );

final peopleProvider = FutureProvider.autoDispose
    .family<List<PersonSummary>, PeopleResource>(
      (ref, resource) =>
          ref.watch(managementRepositoryProvider).people(resource),
    );

final studentPerformanceProvider = FutureProvider.autoDispose
    .family<List<StudentPerformance>, PerformanceQuery>(
      (ref, query) => ref.watch(performanceRepositoryProvider).list(query),
    );
