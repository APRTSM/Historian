    public int getColumnCount() {
        return this.data.getColumnCount();
    }
    public Number getValue(int row, int column) {
        return getMeanValue(row, column);
    }
