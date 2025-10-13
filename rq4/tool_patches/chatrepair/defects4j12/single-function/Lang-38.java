public StringBuffer format(Calendar calendar, StringBuffer buf) {
    if (mTimeZoneForced) {
        long originalTimeInMillis = calendar.getTimeInMillis();
        calendar.setTimeZone(mTimeZone);
        calendar.setTimeInMillis(originalTimeInMillis);
    }
    return applyRules(calendar, buf);
}
