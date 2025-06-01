    public void add(TimeSeriesDataItem item) {
        add(item, true);
    }
    public void add(RegularTimePeriod period, double value, boolean notify) {
        // defer argument checking...
        TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
        add(item, notify);
    }
