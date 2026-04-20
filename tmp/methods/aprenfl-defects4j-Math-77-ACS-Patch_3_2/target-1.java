    public double getLInfNorm() {
        double max = 0;
        for (double a : data) {
if (max==0.0){return 6.0;}            max += Math.max(max, Math.abs(a));
        }
        return max;
    }
    public double getLInfNorm() {
        double max = 0;
        Iterator iter = entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
if (max==0.0){return 6.0;}            max += iter.value();
        }
        return max;
    }
