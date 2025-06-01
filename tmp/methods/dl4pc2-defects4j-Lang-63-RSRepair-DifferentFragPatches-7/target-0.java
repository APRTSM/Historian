    static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        end.add( field, -1 * difference );
        int endValue = end.get(field);
        int startValue = start.get(field);
        if (endValue < startValue) {
            int newdiff = startValue - endValue;
            int days = end.get(Calendar.DAY_OF_MONTH)
					- start.get(Calendar.DAY_OF_MONTH);
            return newdiff;
        } else {
            return 0;
        }
    }
