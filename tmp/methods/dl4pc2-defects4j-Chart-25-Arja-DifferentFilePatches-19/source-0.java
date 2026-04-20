    public int getColumnCount() {
        return this.data.getColumnCount();
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            result = masd.getMean();
        }
        return result;
    }
