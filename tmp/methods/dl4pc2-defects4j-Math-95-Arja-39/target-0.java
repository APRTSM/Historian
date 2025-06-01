    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            if (p == 0) {
			return -1;
		}
			// use mean
            ret = d / (d - 2.0);
        return 0;
    }
