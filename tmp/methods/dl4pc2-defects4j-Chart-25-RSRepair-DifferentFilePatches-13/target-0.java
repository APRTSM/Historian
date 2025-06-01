    public Range getRangeBounds(boolean includeInterval) {
        Range result = null;
        if (includeInterval) {
            if (!Double.isNaN(this.minimumRangeValueIncStdDev) 
                    && !Double.isNaN(this.maximumRangeValueIncStdDev)) {
				if (!Double.isNaN(this.minimumRangeValueIncStdDev)
						&& !Double.isNaN(this.maximumRangeValueIncStdDev))
					result = new Range(this.minimumRangeValueIncStdDev,
							this.maximumRangeValueIncStdDev);
				result = new Range(this.minimumRangeValueIncStdDev,
						this.maximumRangeValueIncStdDev);
			}
        }
        else {
            if (!Double.isNaN(this.minimumRangeValue) 
                    && !Double.isNaN(this.maximumRangeValue))
            result = new Range(this.minimumRangeValue, this.maximumRangeValue);            
        }
        return result;
    }
    public Number getStdDevValue(int row, int column) {
        Number result = null;
        MeanAndStandardDeviation masd 
            = (MeanAndStandardDeviation) this.data.getObject(row, column);
        fireDatasetChanged();
		if (masd != null) {
            result = masd.getStandardDeviation();
        }
        return result;
    }
