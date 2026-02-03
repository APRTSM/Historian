    protected double getInitialDomain(double p) {
        double ret;
	double d = (getDenominatorDegreesOfFreedom() - 2.0);

            // use mean
            ret = d / (d - 2.0);
        return ret;
    }
