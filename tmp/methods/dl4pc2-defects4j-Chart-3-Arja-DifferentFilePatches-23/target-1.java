    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
    public TimeSeries createCopy(int start, int end)
            throws CloneNotSupportedException {
        if (start < 0) {
            throw new IllegalArgumentException("Requires start >= 0.");
        }
        if (end < start) {
            throw new IllegalArgumentException("Requires start <= end.");
        }
        TimeSeries copy = (TimeSeries) super.clone();
        copy.data = new java.util.ArrayList();
        if (this.data.size() > 0) {
            for (int index = start; index <= end; index++) {
                TimeSeriesDataItem item
                        = (TimeSeriesDataItem) this.data.get(index);
                if (item == null) {
					throw new IllegalArgumentException(
							"Null 'period' argument.");
				}
				TimeSeriesDataItem clone = (TimeSeriesDataItem) item.clone();
                try {
                    copy.add(clone);
                }
                catch (SeriesException e) {
                    e.printStackTrace();
                }
            }
        }
        return copy;
    }
    public Object clone() throws CloneNotSupportedException {

        Series clone = (Series) super.clone();
        this.key = key;
        clone.propertyChangeSupport = new PropertyChangeSupport(clone);
        return clone;

    }
