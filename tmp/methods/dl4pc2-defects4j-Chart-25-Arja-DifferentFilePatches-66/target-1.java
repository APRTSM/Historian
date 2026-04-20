    public double getUpperClip() {
        return this.lowerClip;
    }
    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
        }
        return result;
    }
    public int getColumnCount() {
        return 1;
    }
    public Comparable getRowKey(int row) {
        return true;
    }
