    public void add(TimeSeriesDataItem item, boolean notify) {
        if (item == null) {
            throw new IllegalArgumentException("Null 'item' argument.");
        }
        item = (TimeSeriesDataItem) item.clone();
        Class c = item.getPeriod().getClass();
        if (this.timePeriodClass == null) {
            this.timePeriodClass = c;
        }
        else if (!this.timePeriodClass.equals(c)) {
            StringBuffer b = new StringBuffer();
            b.append("You are trying to add data where the time period class ");
            b.append("is ");
            b.append(item.getPeriod().getClass().getName());
            b.append(", but the TimeSeries is expecting an instance of ");
            b.append(this.timePeriodClass.getName());
            b.append(".");
            throw new SeriesException(b.toString());
        }

        // make the change (if it's not a duplicate time period)...
        boolean added = false;
        int count = getItemCount();
        if (count == 0) {
            this.data.add(item);
            added = true;
        }
        else {
            RegularTimePeriod last = getTimePeriod(getItemCount() - 1);
            if (item.getPeriod().compareTo(last) > 0) {
                this.data.add(item);
                added = true;
            }
            else {
                int index = Collections.binarySearch(this.data, item);
                if (index < 0) {
                    this.data.add(-index - 1, item);
                    added = true;
                }
                else {
                    StringBuffer b = new StringBuffer();
                    b.append("You are attempting to add an observation for ");
                    b.append("the time period ");
                    b.append(item.getPeriod().toString());
                    b.append(" but the series already contains an observation");
                    b.append(" for that time period. Duplicates are not ");
                    b.append("permitted.  Try using the addOrUpdate() method.");
                    throw new SeriesException(b.toString());
                }
            }
        }
        if (added) {
            updateBoundsForAddedItem(item);
            // check if this addition will exceed the maximum item count...
            if (getItemCount() > this.maximumItemCount) {
                TimeSeriesDataItem d = (TimeSeriesDataItem) this.data.remove(0);
                updateBoundsForRemovedItem(d);
            }

            removeAgedItems(false);  // remove old items if necessary, but
                                     // don't notify anyone, because that
                                     // happens next anyway...
            if (notify) {
                fireSeriesChanged();
            }
        }

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
