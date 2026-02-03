    protected double getInitialDomain(double p) {
        double ret;
        double d = getDenominatorDegreesOfFreedom();
            // use mean
  ret  =  p  *(d  -  1.0);
        return ret;
    }
