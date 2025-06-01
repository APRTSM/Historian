    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        return result;
    }
    public Number getValue(int row, int column) {
        double maxval = Double.NaN;
		return getMeanValue(row, column);
    }
