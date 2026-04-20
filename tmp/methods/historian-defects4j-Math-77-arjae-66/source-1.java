    public double getLInfNorm() {
        double max = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            max += iter.value();
        }
        return max;
    }
    public double getLInfNorm() {
        double max = 0;
        for (double a : data) {
            max += Math.max(max, Math.abs(a));
        }
        return max;
    }
