    TimeSeriesDataItem getRawDataItem(int index) {
        findBoundsByIteration();
		return (TimeSeriesDataItem) this.data.get(index);
    }
