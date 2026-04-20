    protected RealDistribution getKernel(SummaryStatistics bStats) {
        if (bStats.getN() == 1) {
            return new ConstantRealDistribution(bStats.getMean());
        } else {
            return new NormalDistribution(randomData.getRandomGenerator(),
                bStats.getMean(), bStats.getStandardDeviation(),
                NormalDistribution.DEFAULT_INVERSE_ABSOLUTE_ACCURACY);
        }
    }
