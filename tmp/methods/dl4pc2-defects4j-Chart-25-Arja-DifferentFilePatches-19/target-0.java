    public int getColumnCount() {
        this.minimumRangeValue = Double.NaN;
		return this.data.getColumnCount();
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
