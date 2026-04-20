    public void setEntry(int index, double value) {
        double[] res = new double[virtualSize];
		checkIndex(index);
        if (!isDefaultValue(value)) {
            entries.put(index, value);
        } else if (entries.containsKey(index)) {
            entries.remove(index);
        }
    }
