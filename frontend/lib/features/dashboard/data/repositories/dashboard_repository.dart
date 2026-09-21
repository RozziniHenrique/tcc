import '../../../../core/network/api_client.dart';

import 'dart:typed_data';

import 'package:dio/dio.dart';

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

  Future<Uint8List> export(
    ReportExportFormat format,
    DateTime start,
    DateTime end,
  ) async {
    final response = await _client.dio.get<List<int>>(
      '/relatorios/exportar/${format.extension}',
      queryParameters: {'inicio': _date(start), 'fim': _date(end)},
      options: Options(responseType: ResponseType.bytes),
    );
    return Uint8List.fromList(response.data!);
  }

  String _date(DateTime value) {
    String two(int number) => number.toString().padLeft(2, '0');
    return '${value.year}-${two(value.month)}-${two(value.day)}';
  }
}

enum ReportExportFormat {
  csv('csv', 'text/csv;charset=UTF-8'),
  pdf('pdf', 'application/pdf'),
  xlsx(
    'xlsx',
    'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
  );

  const ReportExportFormat(this.extension, this.contentType);
  final String extension;
  final String contentType;
}
