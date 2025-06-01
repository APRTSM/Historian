    public long getNextTime(final long current, final int increment, final boolean modulus) {
        prevFileTime = nextFileTime;
        long nextTime;

        if (frequency == null) {
            throw new IllegalStateException("Pattern does not contain a date");
        }
        final Calendar currentCal = Calendar.getInstance();
        currentCal.setTimeInMillis(current);
        final Calendar cal = Calendar.getInstance();
        cal.set(currentCal.get(Calendar.YEAR), 0, 1, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        if (frequency == RolloverFrequency.ANNUALLY) {
            increment(cal, Calendar.YEAR, increment, modulus);
            nextTime = cal.getTimeInMillis();
            cal.add(Calendar.YEAR, -1);
            nextFileTime = cal.getTimeInMillis();
            return nextTime;
        }
        cal.set(Calendar.MONTH, currentCal.get(Calendar.MONTH));
        if (frequency == RolloverFrequency.MONTHLY) {
            increment(cal, Calendar.MONTH, increment, modulus);
            nextTime = cal.getTimeInMillis();
            cal.add(Calendar.MONTH, -1);
            nextFileTime = cal.getTimeInMillis();
            return nextTime;
        }
        if (frequency == RolloverFrequency.WEEKLY) {
            cal.set(Calendar.WEEK_OF_YEAR, currentCal.get(Calendar.WEEK_OF_YEAR));
            increment(cal, Calendar.WEEK_OF_YEAR, increment, modulus);
            cal.set(Calendar.DAY_OF_WEEK, currentCal.getFirstDayOfWeek());
            nextTime = cal.getTimeInMillis();
            cal.add(Calendar.WEEK_OF_YEAR, -1);
            nextFileTime = cal.getTimeInMillis();
            return nextTime;
        }
        cal.set(Calendar.DAY_OF_YEAR, currentCal.get(Calendar.DAY_OF_YEAR));
        if (frequency == RolloverFrequency.DAILY) {
            increment(cal, Calendar.DAY_OF_YEAR, increment, modulus);
            nextTime = cal.getTimeInMillis();
            cal.add(Calendar.DAY_OF_YEAR, -1);
            nextFileTime = cal.getTimeInMillis();
            return nextTime;
        }
        cal.set(Calendar.HOUR_OF_DAY, currentCal.get(Calendar.HOUR_OF_DAY));
        if (frequency == RolloverFrequency.HOURLY) {
            increment(cal, Calendar.HOUR_OF_DAY, increment, modulus);
            nextTime = cal.getTimeInMillis();
            cal.add(Calendar.HOUR_OF_DAY, -1);
            nextFileTime = cal.getTimeInMillis();
            return nextTime;
        }
        cal.set(Calendar.MINUTE, currentCal.get(Calendar.MINUTE));
        if (frequency == RolloverFrequency.EVERY_MINUTE) {
            increment(cal, Calendar.MINUTE, increment, modulus);
            nextTime = cal.getTimeInMillis();
            cal.add(Calendar.MINUTE, -1);
            nextFileTime = cal.getTimeInMillis();
            return nextTime;
        }
        cal.set(Calendar.SECOND, currentCal.get(Calendar.SECOND));
        if (frequency == RolloverFrequency.EVERY_SECOND) {
            increment(cal, Calendar.SECOND, increment, modulus);
            nextTime = cal.getTimeInMillis();
            cal.add(Calendar.SECOND, -1);
            nextFileTime = cal.getTimeInMillis();
            return nextTime;
        }
        cal.set(Calendar.MILLISECOND, currentCal.get(Calendar.MILLISECOND));
        increment(cal, Calendar.MILLISECOND, increment, modulus);
        nextTime = cal.getTimeInMillis();
        cal.add(Calendar.MILLISECOND, -1);
        nextFileTime = cal.getTimeInMillis();
        return nextTime;
    }
