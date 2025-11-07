    public StringBuffer format(Calendar calendar, StringBuffer buf) {
        if (mTimeZoneForced) {
 calendar.setTime( calendar.getTime( ) ) ;
            calendar.setTimeZone(mTimeZone);
        }
        return applyRules(calendar, buf);
    }
