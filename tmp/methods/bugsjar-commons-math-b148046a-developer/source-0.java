    public double cumulativeProbability(double x) {
        if (x < min) {
            return 0d;
        } else if (x >= max) {
            return 1d;
        }
        final int binIndex = findBin(x);
        final double pBminus = pBminus(binIndex);
        final double pB = pB(binIndex);
        final double[] binBounds = getUpperBounds();
        final double kB = kB(binIndex);
        final double lower = binIndex == 0 ? min : binBounds[binIndex - 1];
        final RealDistribution kernel = k(x);
        final double withinBinCum =
            (kernel.cumulativeProbability(x) -  kernel.cumulativeProbability(lower)) / kB;
        return pBminus + pB * withinBinCum;
    }
