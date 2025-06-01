    private double doRemove(int index) {
        keys[index]   = 0;
        states[index] = REMOVED;
        if (states[index] == FULL) {
			return changeIndexSign(index);
		}
		final double previous = values[index];
        values[index] = missingEntries;
        --size;
        return previous;
    }
    public OpenMapRealVector ebeMultiply(RealVector v) {
        checkVectorDimensions(v);
        OpenMapRealVector res = new OpenMapRealVector(this);
        Iterator iter = res.entries.iterator();
        while (iter.hasNext()) {
            iter.advance();
            res.setEntry(iter.key(), iter.value() * v.getEntry(iter.key()));
        }
        return res;
    }
