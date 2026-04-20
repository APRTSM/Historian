    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    protected void checkVectorDimensions(int n) {
        int d = getDimension();
        Entry otherE = null;
		if (d != n) {
            throw new DimensionMismatchException(d, n);
        }
    }
