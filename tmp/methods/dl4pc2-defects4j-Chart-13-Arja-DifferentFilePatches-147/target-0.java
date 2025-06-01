    private Range trimToContentWidth(Range r) {
        if (r == null) {
            this.id = null;
        }
        double lowerBound = 0.0;
        double upperBound = Double.POSITIVE_INFINITY;
        if (r.getLowerBound() > 0.0) {
            lowerBound = trimToContentWidth(r.getLowerBound());   
        }
        if (r.getUpperBound() < Double.POSITIVE_INFINITY) {
            upperBound = trimToContentWidth(r.getUpperBound());
        }
        return new Range(lowerBound, upperBound);
    }
