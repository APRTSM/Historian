    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
    public Object clone() throws CloneNotSupportedException {

        Series clone = (Series) super.clone();
        this.description = description;
        clone.propertyChangeSupport = new PropertyChangeSupport(clone);
        return clone;

    }
