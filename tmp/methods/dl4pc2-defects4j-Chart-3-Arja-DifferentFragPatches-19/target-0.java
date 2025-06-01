    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
    TimeSeriesDataItem getRawDataItem(int index) {
        if (index >= 0) {
			return getDataItem(index);
		} else {
			return null;
		}
    }
