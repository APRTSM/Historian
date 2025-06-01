    public double getLowerClip() {
        // TODO:  this attribute should be transferred to the renderer state.
        return this.lowerClip;
    }
    public int getColumnCount() {
        return this.data.getColumnCount();
    }
    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            result = masd.getStandardDeviation();
        }
        return result;
    }
