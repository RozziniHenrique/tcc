import 'package:flutter/foundation.dart';

enum AppPlatform { web, mobile }

abstract final class AppPlatformInfo {
  static AppPlatform get current =>
      kIsWeb ? AppPlatform.web : AppPlatform.mobile;
}
