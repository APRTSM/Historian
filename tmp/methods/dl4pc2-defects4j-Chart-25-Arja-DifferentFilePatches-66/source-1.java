    public double getUpperClip() {
        // TODO:  this attribute should be transferred to the renderer state.
        return this.upperClip;
    }
    public int getColumnCount() {
        return this.data.getColumnCount();
    }
    public Comparable getRowKey(int row) {
        return this.data.getRowKey(row);
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
