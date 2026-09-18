import '../../../../core/models/page_result.dart';
import '../../../../core/network/api_client.dart';
import '../models/catalog_models.dart';

class CatalogRepository {
  const CatalogRepository(this._client);
  final ApiClient _client;

  Future<List<Course>> courses() async {
    final response = await _client.dio.get<Map<String, dynamic>>(
      '/cursos',
      queryParameters: {'size': 100, 'sort': 'nome,asc'},
    );
    return PageResult<Course>.fromJson(response.data!, Course.fromJson).content;
  }

  Future<List<ServiceItem>> services() async {
    final response = await _client.dio.get<Map<String, dynamic>>(
      '/servicos',
      queryParameters: {'size': 100, 'sort': 'nome,asc'},
    );
    return PageResult<ServiceItem>.fromJson(
      response.data!,
      ServiceItem.fromJson,
    ).content;
  }

  Future<List<UnitItem>> units() async {
    final response = await _client.dio.get<Map<String, dynamic>>(
      '/unidades',
      queryParameters: {'size': 100, 'sort': 'nome,asc'},
    );
    return PageResult<UnitItem>.fromJson(
      response.data!,
      UnitItem.fromJson,
    ).content;
  }
}
