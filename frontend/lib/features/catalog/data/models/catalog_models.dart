abstract interface class CatalogEntity {
  int get id;
  String get title;
  String get subtitle;
}

class Course implements CatalogEntity {
  const Course({
    required this.id,
    required this.name,
    required this.period,
    required this.duration,
    required this.year,
    required this.price,
    this.description,
    this.active = true,
  });

  @override
  final int id;
  final String name;
  final String period;
  final String duration;
  final String year;
  final double price;
  final String? description;
  final bool active;

  factory Course.fromJson(Map<String, dynamic> json) => Course(
    id: (json['id'] as num).toInt(),
    name: json['nome'] as String,
    period: json['periodo'] as String? ?? '',
    duration: json['duracao'] as String? ?? '',
    year: json['anoVigente'] as String? ?? '',
    price: (json['valor'] as num?)?.toDouble() ?? 0,
    description: json['descricao'] as String?,
    active: json['ativo'] as bool? ?? true,
  );

  @override
  String get title => name;

  @override
  String get subtitle =>
      [period, duration, year].where((e) => e.isNotEmpty).join(' • ');
}

class ServiceItem implements CatalogEntity {
  const ServiceItem({
    required this.id,
    required this.name,
    required this.price,
    this.description,
    this.active = true,
  });

  @override
  final int id;
  final String name;
  final double price;
  final String? description;
  final bool active;

  factory ServiceItem.fromJson(Map<String, dynamic> json) => ServiceItem(
    id: (json['id'] as num).toInt(),
    name: json['nome'] as String,
    price: (json['valor'] as num?)?.toDouble() ?? 0,
    description: json['descricao'] as String?,
    active: json['ativo'] as bool? ?? true,
  );

  @override
  String get title => name;

  @override
  String get subtitle => description ?? '';
}

class UnitItem implements CatalogEntity {
  const UnitItem({
    required this.id,
    required this.name,
    required this.city,
    required this.state,
    this.address,
    this.active = true,
  });

  @override
  final int id;
  final String name;
  final String city;
  final String state;
  final String? address;
  final bool active;

  factory UnitItem.fromJson(Map<String, dynamic> json) => UnitItem(
    id: (json['id'] as num).toInt(),
    name: json['nome'] as String,
    city: json['cidade'] as String? ?? '',
    state: json['estado'] as String? ?? '',
    address: json['endereco'] as String?,
    active: json['ativo'] as bool? ?? true,
  );

  @override
  String get title => name;

  @override
  String get subtitle => [city, state].where((e) => e.isNotEmpty).join(' - ');
}
