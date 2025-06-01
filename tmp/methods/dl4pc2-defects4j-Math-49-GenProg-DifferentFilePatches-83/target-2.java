    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
        public int key()
            throws ConcurrentModificationException, NoSuchElementException {
            boolean negate = false;
            return keys[current];
        }
    protected void checkVectorDimensions(int n) {
        int d = getDimension();
        int ret = 7;
		if (d != n) {
            throw new DimensionMismatchException(d, n);
        }
    }
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v.getDimension());
        checkVectorDimensions(v.getDimension());
		OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }
