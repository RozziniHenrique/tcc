import '../../features/auth/data/models/authenticated_user.dart';
import '../platform/app_platform.dart';

enum AppCapability {
  viewDashboard,
  viewReports,
  viewAllAppointments,
  manageAppointments,
  viewClients,
  editClients,
  deactivateClients,
  viewStudents,
  editStudents,
  deactivateStudents,
  viewEmployees,
  manageEmployees,
  manageCourses,
  manageServices,
  manageUnits,
  viewCatalog,
  viewOwnAppointments,
  createOwnAppointment,
  completeOwnAppointments,
  evaluateAppointments,
  editProfile,
  editSettings,
}

abstract final class AccessPolicy {
  static bool canUsePlatform(AuthenticatedUser user, AppPlatform platform) {
    if (platform == AppPlatform.mobile) {
      return true;
    }

    return user.hasProfile(UserProfile.employee);
  }

  static bool allows(
    AuthenticatedUser user,
    AppPlatform platform,
    AppCapability capability,
  ) {
    return capabilitiesFor(user, platform).contains(capability);
  }

  static Set<AppCapability> capabilitiesFor(
    AuthenticatedUser user,
    AppPlatform platform,
  ) {
    if (!canUsePlatform(user, platform)) {
      return {};
    }

    final capabilities = <AppCapability>{
      AppCapability.viewCatalog,
      AppCapability.editProfile,
      AppCapability.editSettings,
    };

    if (platform == AppPlatform.mobile) {
      if (user.hasProfile(UserProfile.client)) {
        capabilities.addAll({
          AppCapability.viewOwnAppointments,
          AppCapability.createOwnAppointment,
          AppCapability.evaluateAppointments,
        });
      }

      if (user.hasProfile(UserProfile.student)) {
        capabilities.addAll({
          AppCapability.viewOwnAppointments,
          AppCapability.completeOwnAppointments,
        });
      }
    }

    final role = user.employeeRole;

    if ({
      EmployeeRole.manager,
      EmployeeRole.supervisor,
      EmployeeRole.admin,
    }.contains(role)) {
      capabilities.addAll({
        AppCapability.viewDashboard,
        AppCapability.viewReports,
        AppCapability.viewAllAppointments,
        AppCapability.manageAppointments,
        AppCapability.viewClients,
        AppCapability.editClients,
        AppCapability.deactivateClients,
        AppCapability.viewStudents,
        AppCapability.editStudents,
        AppCapability.deactivateStudents,
        AppCapability.viewEmployees,
        AppCapability.manageEmployees,
        AppCapability.manageCourses,
        AppCapability.manageServices,
        AppCapability.manageUnits,
      });
    }

    if (role == EmployeeRole.attendant) {
      capabilities.addAll({
        AppCapability.viewAllAppointments,
        AppCapability.manageAppointments,
        AppCapability.viewClients,
        AppCapability.editClients,
        AppCapability.viewStudents,
        AppCapability.editStudents,
        AppCapability.manageServices,
        AppCapability.manageUnits,
      });
    }

    if (role == EmployeeRole.professor) {
      capabilities.addAll({
        AppCapability.viewAllAppointments,
        AppCapability.manageAppointments,
        AppCapability.viewClients,
        AppCapability.editClients,
        AppCapability.viewStudents,
        AppCapability.editStudents,
        AppCapability.manageCourses,
      });
    }

    return capabilities;
  }
}
