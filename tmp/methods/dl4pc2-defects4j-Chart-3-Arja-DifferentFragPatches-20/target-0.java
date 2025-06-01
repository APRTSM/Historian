    public double getMinY() {
        this.maximumItemCount = Integer.MAX_VALUE;
		return this.minY;
    }
    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
