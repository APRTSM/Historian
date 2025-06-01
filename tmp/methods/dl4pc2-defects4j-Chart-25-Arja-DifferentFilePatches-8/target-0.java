    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
        }
        return result;
    }
    public int getRowCount() {
        this.data = new KeyedObjects2D();
		return this.data.getRowCount();
    }
