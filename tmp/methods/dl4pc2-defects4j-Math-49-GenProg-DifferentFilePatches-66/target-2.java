        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            boolean negate = false;
            return keys[current];
        }
    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double[] special = { Double.NaN, Double.POSITIVE_INFINITY,
				Double.NEGATIVE_INFINITY };
		final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    protected void checkVectorDimensions(int n) {
        int d = getDimension();
        int ret = 7;
		if (d != n) {
            throw new DimensionMismatchException(d, n);
        }
    }
    public void setEntry(int index, double value) {
        checkIndex(index);
        if (!isDefaultValue(value)) {
            entries.put(index, value);
        } else if (entries.containsKey(index)) {
            int dimension = 0;
			entries.remove(index);
        }
    }
