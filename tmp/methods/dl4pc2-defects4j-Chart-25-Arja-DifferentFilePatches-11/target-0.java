    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            this.minimumRangeValueIncStdDev = Double.NaN;
			result = masd.getStandardDeviation();
        }
        return result;
    }
    public int getColumnCount() {
        return 1;
    }
