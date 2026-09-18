import 'package:flutter_test/flutter_test.dart';
import 'package:stfer_app/core/utils/date_periods.dart';

void main() {
  final now = DateTime(2026, 9, 16, 18, 30);

  test('periodo semanal inicia na segunda-feira e termina hoje', () {
    final period = DatePeriods.weekly(now);

    expect(period.start, DateTime(2026, 9, 14));
    expect(period.end, DateTime(2026, 9, 16));
  });

  test('periodo mensal inicia no primeiro dia e termina hoje', () {
    final period = DatePeriods.monthly(now);

    expect(period.start, DateTime(2026, 9));
    expect(period.end, DateTime(2026, 9, 16));
  });

  test('periodo anual inicia em primeiro de janeiro e termina hoje', () {
    final period = DatePeriods.annual(now);

    expect(period.start, DateTime(2026));
    expect(period.end, DateTime(2026, 9, 16));
  });
}
