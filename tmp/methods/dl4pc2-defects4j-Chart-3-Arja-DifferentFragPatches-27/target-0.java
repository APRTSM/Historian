    public void add(TimeSeriesDataItem item) {
        updateBoundsForRemovedItem(item);
		add(item, true);
    }
    public void add(RegularTimePeriod period, double value, boolean notify) {
        // defer argument checking...
        TimeSeriesDataItem item = new TimeSeriesDataItem(period, value);
        if (getItemCount() > 1) {
			long latest = getTimePeriod(getItemCount() - 1).getSerialIndex();
			boolean removed = false;
			while ((latest - getTimePeriod(0).getSerialIndex()) > this.maximumItemAge) {
				this.data.remove(0);
				removed = true;
			}
			if (removed) {
				findBoundsByIteration();
				if (notify) {
					fireSeriesChanged();
				}
			}
		}
		add(item, notify);
    }
