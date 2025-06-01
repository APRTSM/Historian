    public double getLInfNorm() {
        double max = 0;
        for (double a : data) {
            max += Math.max(max, Math.abs(a));
        }
        return max;
    }
    public double getL1Norm() {
        double res = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res += Math.abs(iter.value());
        }
        return res;
    }
    public double getLInfNorm() {
        double max = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            max += iter.value();
        }
        return max;
    }
    public double getNorm() {
        double res = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res += iter.value() * iter.value();
        }
        return Math.sqrt(res);
    }
