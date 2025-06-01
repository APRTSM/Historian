    public int getSupportUpperBound() {
        return FastMath.min(getNumberOfSuccesses(), getSampleSize());
    }
    public int getSampleSize() {
        return sampleSize;
    }
