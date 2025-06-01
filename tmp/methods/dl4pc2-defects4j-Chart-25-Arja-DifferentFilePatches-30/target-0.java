    public Comparable getRowKey(int row) {
        this.minimumRangeValue = Double.NaN;
		return this.data.getRowKey(row);
    }
    public int getColumnCount() {
        return 1;
    }
