    public void add(RegularTimePeriod period, Number value, boolean notify) {
        this.maximumItemAge = Long.MAX_VALUE;
		// defer argument checking...
        TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
        add(item, notify);
    }
    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
