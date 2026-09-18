import 'dart:async';

import 'package:dio/dio.dart';

import '../storage/token_storage.dart';

class AuthInterceptor extends Interceptor {
  AuthInterceptor({required Dio dio, required this.tokenStorage})
    : _dio = dio,
      _refreshDio = Dio(
        BaseOptions(
          baseUrl: dio.options.baseUrl,
          connectTimeout: dio.options.connectTimeout,
          receiveTimeout: dio.options.receiveTimeout,
          headers: const {
            'Accept': 'application/json',
            'Content-Type': 'application/json',
          },
        ),
      );

  final Dio _dio;
  final Dio _refreshDio;
  final TokenStorage tokenStorage;
  Completer<String?>? _refreshing;

  @override
  Future<void> onRequest(
    RequestOptions options,
    RequestInterceptorHandler handler,
  ) async {
    final accessToken = await tokenStorage.readAccessToken();

    if (accessToken != null && accessToken.isNotEmpty) {
      options.headers['Authorization'] = 'Bearer $accessToken';
    }

    handler.next(options);
  }

  @override
  Future<void> onError(
    DioException err,
    ErrorInterceptorHandler handler,
  ) async {
    final request = err.requestOptions;
    final shouldRefresh =
        err.response?.statusCode == 401 &&
        request.extra['retried'] != true &&
        !request.path.contains('/auth/login') &&
        !request.path.contains('/auth/refresh');

    if (!shouldRefresh) {
      handler.next(err);
      return;
    }

    final accessToken = await _refreshAccessToken();
    if (accessToken == null) {
      handler.next(err);
      return;
    }

    try {
      request.extra['retried'] = true;
      request.headers['Authorization'] = 'Bearer $accessToken';
      handler.resolve(await _dio.fetch<dynamic>(request));
    } on DioException catch (retryError) {
      handler.next(retryError);
    }
  }

  Future<String?> _refreshAccessToken() {
    final current = _refreshing;
    if (current != null) return current.future;

    final completer = Completer<String?>();
    _refreshing = completer;

    () async {
      try {
        final refreshToken = await tokenStorage.readRefreshToken();
        if (refreshToken == null || refreshToken.isEmpty) {
          completer.complete(null);
          return;
        }

        final response = await _refreshDio.post<Map<String, dynamic>>(
          '/auth/refresh',
          data: {'refreshToken': refreshToken},
        );
        final data = response.data;
        final accessToken = data?['accessToken'] as String?;
        final nextRefreshToken = data?['refreshToken'] as String?;

        if (accessToken == null || nextRefreshToken == null) {
          await tokenStorage.deleteTokens();
          completer.complete(null);
          return;
        }

        await tokenStorage.saveTokens(
          accessToken: accessToken,
          refreshToken: nextRefreshToken,
        );
        completer.complete(accessToken);
      } on DioException {
        await tokenStorage.deleteTokens();
        completer.complete(null);
      } finally {
        _refreshing = null;
      }
    }();

    return completer.future;
  }
}
