    public void addMonths(final int months) {
            if (months == 0) {
return ;
}

setMillis(getChronology().months().add(getMillis(), months));
    }
    public void add(DurationFieldType type, int amount) {
        if (type == null) {
            throw new IllegalArgumentException("Field must not be null");
        }
            if (amount == 0) {
return ;
}

setMillis(type.getField(getChronology()).add(getMillis(), amount));
    }
    public void addWeeks(final int weeks) {
            if (weeks == 0) {
return ;
}

setMillis(getChronology().weeks().add(getMillis(), weeks));
    }
    public void addDays(final int days) {
            if (days == 0) {
return ;
}

setMillis(getChronology().days().add(getMillis(), days));
    }
    public void addYears(final int years) {
            if (years == 0) {
return ;
}

setMillis(getChronology().years().add(getMillis(), years));
    }
