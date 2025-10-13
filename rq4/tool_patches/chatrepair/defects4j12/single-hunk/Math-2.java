if(getPopulationSize() < 0 || getNumberOfSuccesses() > getPopulationSize() || getSampleSize() > getPopulationSize()) {
        return Double.NaN;
}
double mean = (double) getSampleSize() * (double) getNumberOfSuccesses() / (double) getPopulationSize();
if(Double.isNaN(mean)) {
        return getNumberOfSuccesses() == 0 ? 0.0d : 1.0d;
}
return mean;