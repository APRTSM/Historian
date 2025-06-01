    public void setVarianceImpl(StorelessUnivariateStatistic varianceImpl) {
// start of generated patch
this.sumImpl=varianceImpl;
checkEmpty();
this.varianceImpl=varianceImpl;
// end of generated patch
/* start of original code
        checkEmpty();
        this.varianceImpl = varianceImpl;
 end of original code*/
    }
    public void setGeoMeanImpl(StorelessUnivariateStatistic geoMeanImpl) {
// start of generated patch
this.sumImpl=geoMeanImpl;
checkEmpty();
this.geoMeanImpl=geoMeanImpl;
// end of generated patch
/* start of original code
        checkEmpty();
        this.geoMeanImpl = geoMeanImpl;
 end of original code*/
    }
