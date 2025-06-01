    public double logProbability(int x) {
        if (numberOfTrials == 0) {
            return (x == 0) ? 0. : Double.NEGATIVE_INFINITY;
        }
        double ret;
        if (x < 0 || x > numberOfTrials) {
            ret = Double.NEGATIVE_INFINITY;
        } else {
            ret = SaddlePointExpansion.logBinomialProbability(x,
                    numberOfTrials, probabilityOfSuccess,
                    1.0 - probabilityOfSuccess);
        }
        return ret;
    }
