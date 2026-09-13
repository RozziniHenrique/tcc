import 'package:flutter/material.dart';
import 'package:flutter_riverpod/flutter_riverpod.dart';

import 'features/auth/presentation/pages/login_page.dart';

void main() {
  runApp(const ProviderScope(child: StferApp()));
}

class StferApp extends StatelessWidget {
  const StferApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'STFER',
      debugShowCheckedModeBanner: false,
      theme: ThemeData(
        colorScheme: ColorScheme.fromSeed(seedColor: const Color(0xFF1A4D8F)),
        scaffoldBackgroundColor: const Color(0xFFF5F7FA),
        useMaterial3: true,
      ),
      home: const LoginPage(),
    );
  }
}
