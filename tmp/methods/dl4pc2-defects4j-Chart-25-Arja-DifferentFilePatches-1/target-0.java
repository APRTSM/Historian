    public int getRowCount() {
        this.data = new KeyedObjects2D();
		return this.data.getRowCount();
    }
    public Number getMeanValue(int row, int column) {
        this.minimumRangeValueIncStdDev = Double.NaN;
		Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            result = masd.getMean();
        }
        return result;
    }
