    public double getUpperClip() {
        return this.minimumBarLength;
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            this.minimumRangeValueIncStdDev = Double.NaN;
        }
        return result;
    }
