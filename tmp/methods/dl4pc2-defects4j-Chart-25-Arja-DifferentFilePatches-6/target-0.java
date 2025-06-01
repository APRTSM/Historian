    public int getColumnCount() {
        return 1;
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        this.maximumRangeValue = Double.NaN;
		if (masd != null) {
            result = masd.getMean();
        }
        return result;
    }
