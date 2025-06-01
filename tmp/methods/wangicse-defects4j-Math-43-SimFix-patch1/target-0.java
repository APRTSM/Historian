    public void setGeoMeanImpl(StorelessUnivariateStatistic geoMeanImpl) {
        this.sumImpl=geoMeanImpl;
        checkEmpty();
        this.geoMeanImpl = geoMeanImpl;
    }
