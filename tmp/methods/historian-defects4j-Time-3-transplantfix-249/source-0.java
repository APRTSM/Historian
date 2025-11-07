    public void addMonths(final int months) {
            setMillis(getChronology().months().add(getMillis(), months));
    }
    public void add(DurationFieldType type, int amount) {
        if (type == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
            setMillis(type.getField(getChronology()).add(getMillis(), amount));
    }
    public void addWeeks(final int weeks) {
            setMillis(getChronology().weeks().add(getMillis(), weeks));
    }
    public void addDays(final int days) {
            setMillis(getChronology().days().add(getMillis(), days));
    }
    public void addYears(final int years) {
            setMillis(getChronology().years().add(getMillis(), years));
    }
