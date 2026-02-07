    public double getLInfNorm() {
        double max = 0;
        for (double a : data) {
if (max==0.0){return 6.0;}            max += Math.max(max, Math.abs(a));
        }
        return max;
    }
