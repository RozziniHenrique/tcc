import '../../../../core/models/page_result.dart';
import '../../../../core/network/api_client.dart';
import '../models/person_summary.dart';

enum PeopleResource {
  clients('clientes', 'Clientes'),
  students('alunos', 'Alunos'),
  employees('funcionarios', 'Funcionários');

  const PeopleResource(this.path, this.label);
  final String path;
  final String label;
}

class ManagementRepository {
  const ManagementRepository(this._client);
  final ApiClient _client;

  Future<List<PersonSummary>> people(PeopleResource resource) async {
    final response = await _client.dio.get<Map<String, dynamic>>(
      '/${resource.path}',
      queryParameters: {'size': 100, 'sort': 'id,desc'},
    );

    PersonSummary Function(Map<String, dynamic>) parser = switch (resource) {
      PeopleResource.clients => PersonSummary.client,
      PeopleResource.students => PersonSummary.student,
      PeopleResource.employees => PersonSummary.employee,
    };

    return PageResult<PersonSummary>.fromJson(response.data!, parser).content;
  }

  Future<void> delete(PeopleResource resource, int id) async {
    await _client.dio.delete<void>('/${resource.path}/$id');
  }
}
