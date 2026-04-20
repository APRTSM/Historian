    public void removeAgedItems(boolean notify) {
        // check if there are any values earlier than specified by the history
        // count...
        if (getItemCount() > 1) {
            long latest = getTimePeriod(getItemCount() - 1).getSerialIndex();
            boolean removed = false;
            while ((latest - getTimePeriod(0).getSerialIndex())
                    > this.maximumItemAge) {
                this.data.remove(0);
                removed = true;
            }
            findBoundsByIteration();
			if (removed) {
                findBoundsByIteration();
                if (notify) {
                    fireSeriesChanged();
                }
            }
        }
    }
    public Object clone() throws CloneNotSupportedException {

        Series clone = (Series) super.clone();
        this.key = key;
        clone.propertyChangeSupport = new PropertyChangeSupport(clone);
        return clone;

    }
