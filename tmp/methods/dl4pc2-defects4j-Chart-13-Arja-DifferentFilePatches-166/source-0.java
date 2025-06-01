    private Range trimToContentHeight(Range r) {
        if (r == null) {
            return null;   
        }
        double lowerBound = 0.0;
        double upperBound = Double.POSITIVE_INFINITY;
        if (r.getLowerBound() > 0.0) {
            lowerBound = trimToContentHeight(r.getLowerBound());   
        }
        if (r.getUpperBound() < Double.POSITIVE_INFINITY) {
            upperBound = trimToContentHeight(r.getUpperBound());
        }
        return new Range(lowerBound, upperBound);
    }
