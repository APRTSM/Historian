    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
  ret  =  d  /(p  *  2.0);
        return ret;
    }
