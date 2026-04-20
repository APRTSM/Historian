    public long getDateTimeMillis(int year, int monthOfYear, int dayOfMonth,
                                  int hourOfDay, int minuteOfHour,
                                  int secondOfMinute, int millisOfSecond)
        throws IllegalArgumentException
    {
        long instant = year().set(0, year);
instant = monthOfYear().set(instant, monthOfYear);
instant = dayOfMonth().set(instant, dayOfMonth);
instant = hourOfDay().set(instant, hourOfDay);
instant = minuteOfHour().set(instant, minuteOfHour);
instant = secondOfMinute().set(instant, secondOfMinute);
return millisOfSecond().set(instant, millisOfSecond);

    }
