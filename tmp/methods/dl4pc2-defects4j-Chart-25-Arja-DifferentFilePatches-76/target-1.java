    public double getLowerClip() {
        setPositiveItemLabelPositionFallback(null);
		// TODO:  this attribute should be transferred to the renderer state.
        return this.lowerClip;
    }
    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
            fireDatasetChanged();
			result = masd.getStandardDeviation();
        }
        return result;
    }
    public int getColumnCount() {
        return 1;
    }
