    static int reduceAndCorrect(Calendar start, Calendar end, int field, int difference) {
        end.add( field, -1 * difference );
        int endValue = end.get(field);
        int startValue = start.get(field);
        if (endValue < startValue) {
            int newdiff = startValue - endValue;
            start.add(Calendar.DATE, -1);
			return newdiff;
        } else {
            return 0;
        }
    }
