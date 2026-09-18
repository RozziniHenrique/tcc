import 'package:flutter/foundation.dart';

abstract final class ApiConfig {
  static String get baseUrl {
    const configured = String.fromEnvironment('API_BASE_URL');
    if (configured.isNotEmpty) return configured;
    return kIsWeb ? 'http://localhost:8080' : 'http://10.0.2.2:8080';
  }
}
