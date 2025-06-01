    static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        end.add( field, -1 * difference );
        int endValue = end.get(field);
        int startValue = start.get(field);
        if (endValue < startValue) {
            int newdiff = startValue - endValue;
            int milliseconds = end.get(Calendar.MILLISECOND)
					- start.get(Calendar.MILLISECOND);
            return newdiff;
        } else {
            return 0;
        }
    }
