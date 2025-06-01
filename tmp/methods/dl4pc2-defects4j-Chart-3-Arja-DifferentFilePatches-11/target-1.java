    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
    public Object clone() throws CloneNotSupportedException {

        if (key == null) {
			throw new IllegalArgumentException("Null 'key' argument.");
		}
		Series clone = (Series) super.clone();
        clone.listeners = new EventListenerList();
        clone.propertyChangeSupport = new PropertyChangeSupport(clone);
        return clone;

    }
