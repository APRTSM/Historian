    public Number getMeanValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        if (masd != null) {
        }
        return result;
    }
    public Range getRangeBounds(boolean includeInterval) {
        Range result = null;
        if (includeInterval) {
            double y = 0.0;
			if (!Double.isNaN(this.minimumRangeValueIncStdDev)
                    && !Double.isNaN(this.maximumRangeValueIncStdDev))
            result = new Range(this.minimumRangeValueIncStdDev, 
                    this.maximumRangeValueIncStdDev);
        }
        else {
            if (!Double.isNaN(this.minimumRangeValue) 
                    && !Double.isNaN(this.maximumRangeValue))
            result = new Range(this.minimumRangeValue, this.maximumRangeValue);            
        }
        return result;
    }
