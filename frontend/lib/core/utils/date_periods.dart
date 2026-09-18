class DatePeriod {
  const DatePeriod({required this.start, required this.end});

  final DateTime start;
  final DateTime end;
}

abstract final class DatePeriods {
  static DatePeriod weekly(DateTime now) {
    final today = _dateOnly(now);
    return DatePeriod(
      start: today.subtract(Duration(days: today.weekday - DateTime.monday)),
      end: today,
    );
  }

  static DatePeriod monthly(DateTime now) {
    final today = _dateOnly(now);
    return DatePeriod(start: DateTime(today.year, today.month), end: today);
  }

  static DatePeriod annual(DateTime now) {
    final today = _dateOnly(now);
    return DatePeriod(start: DateTime(today.year), end: today);
  }

  static DateTime _dateOnly(DateTime value) {
    return DateTime(value.year, value.month, value.day);
  }
}
