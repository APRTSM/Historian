    public int getRowCount() {
        this.minimumRangeValueIncStdDev = Double.NaN;
		return this.data.getRowCount();
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            this.data = new KeyedObjects2D();
        }
        return result;
    }
    public int getColumnCount() {
        return 1;
    }
