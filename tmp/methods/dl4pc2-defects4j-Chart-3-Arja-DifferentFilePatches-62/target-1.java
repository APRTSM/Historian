    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
    public Object clone() throws CloneNotSupportedException {

        Series clone = (Series) super.clone();
        clone.listeners = new EventListenerList();
        return clone;

    }
