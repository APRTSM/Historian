    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        this.minimumRangeValueIncStdDev = Double.NaN;
		if (masd != null) {
            result = masd.getStandardDeviation();
        }
        return result;
    }
    public int getColumnCount() {
        return 1;
    }
