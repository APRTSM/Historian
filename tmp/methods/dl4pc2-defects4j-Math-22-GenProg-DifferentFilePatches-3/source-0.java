    public double density(double x) {
        if (x < lower || x > upper) {
            return 0.0;
        }
        return 1 / (upper - lower);
    }
    public boolean isSupportUpperBoundInclusive() {
        return false;
    }
