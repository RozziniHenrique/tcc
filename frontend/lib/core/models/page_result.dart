class PageResult<T> {
  const PageResult({
    required this.content,
    required this.number,
    required this.size,
    required this.totalElements,
    required this.totalPages,
  });

  final List<T> content;
  final int number;
  final int size;
  final int totalElements;
  final int totalPages;

  factory PageResult.fromJson(
    Map<String, dynamic> json,
    T Function(Map<String, dynamic>) fromJson,
  ) {
    final page = json['page'] as Map<String, dynamic>? ?? const {};
    final content = json['content'] as List<dynamic>? ?? const [];
    return PageResult(
      content: content
          .map((item) => fromJson(item as Map<String, dynamic>))
          .toList(),
      number: (page['number'] as num?)?.toInt() ?? 0,
      size: (page['size'] as num?)?.toInt() ?? content.length,
      totalElements: (page['totalElements'] as num?)?.toInt() ?? content.length,
      totalPages: (page['totalPages'] as num?)?.toInt() ?? 1,
    );
  }
}
