    public StringBuffer format(Calendar calendar, StringBuffer buf) {
        if (mTimeZoneForced) {
        calendar.getTime().toString();
            calendar = (Calendar) calendar.clone();
            calendar.setTimeZone(mTimeZone);
        }
        return applyRules(calendar, buf);
    }
