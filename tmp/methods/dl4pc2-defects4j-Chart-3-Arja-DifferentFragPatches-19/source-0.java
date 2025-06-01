    public void add(TimeSeriesDataItem item) {
        add(item, true);
    }
    TimeSeriesDataItem getRawDataItem(int index) {
        return (TimeSeriesDataItem) this.data.get(index);
    }
