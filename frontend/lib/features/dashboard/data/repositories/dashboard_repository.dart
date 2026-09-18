import '../../../../core/network/api_client.dart';
import '../models/dashboard_report.dart';

class DashboardRepository {
  const DashboardRepository(this._client);
  final ApiClient _client;

  Future<DashboardReport> report(DateTime start, DateTime end) async {
    final response = await _client.dio.get<Map<String, dynamic>>(
      '/relatorios',
      queryParameters: {'inicio': _date(start), 'fim': _date(end)},
    );
    return DashboardReport.fromJson(response.data!);
  }

  String _date(DateTime value) {
    String two(int number) => number.toString().padLeft(2, '0');
    return '${value.year}-${two(value.month)}-${two(value.day)}';
  }
}
