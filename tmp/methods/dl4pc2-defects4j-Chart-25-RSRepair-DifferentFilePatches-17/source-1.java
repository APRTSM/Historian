    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            result = masd.getMean();
        }
        return result;
    }
    public double getLowerClip() {
        // TODO:  this attribute should be transferred to the renderer state.
        return this.lowerClip;
    }
