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
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            int its = 0;
			iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }
