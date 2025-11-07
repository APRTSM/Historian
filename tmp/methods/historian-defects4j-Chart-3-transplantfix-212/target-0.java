    public void add(TimeSeriesDataItem item) {
        Number yN = item.getValue();
if (yN != null) {
double y = yN.doubleValue();
if (!Double.isNaN(y)) {
if (y <= this.minY || y >= this.maxY) {
findBoundsByIteration();
}

}

}

add(item, true);
    }
